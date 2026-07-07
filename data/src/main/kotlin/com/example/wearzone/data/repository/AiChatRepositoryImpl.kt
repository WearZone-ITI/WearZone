package com.example.wearzone.data.repository

import com.example.wearzone.data.remote.ai.chat.IAiChatRemoteDataSource
import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.ai.chat.model.ChatProductCard
import com.example.wearzone.domain.ai.chat.model.ChatRole
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.model.ProductVariant
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.ai.chat.repository.IAiChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: IAiChatRemoteDataSource,
    private val productRepository: IProductRepository,
) : IAiChatRepository {

    private val _history = MutableStateFlow<List<ChatMessage>>(emptyList())

    override fun getChatHistory(): Flow<List<ChatMessage>> = _history.asStateFlow()

    override suspend fun clearHistory() {
        _history.value = emptyList()
    }

    override suspend fun sendMessage(message: String): DataResult<Unit> {
        val trimmedMessage = message.trim()
        if (trimmedMessage.isBlank()) return DataResult.Success(Unit)

        val historySnapshot = _history.value
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatRole.USER,
            content = trimmedMessage,
            timestamp = System.currentTimeMillis(),
        )
        val pendingMsgId = UUID.randomUUID().toString()
        val pendingMessage = ChatMessage(
            id = pendingMsgId,
            role = ChatRole.MODEL,
            content = "...",
            timestamp = System.currentTimeMillis(),
            isPending = true,
        )

        _history.value = historySnapshot + listOf(userMessage, pendingMessage)

        return try {
            val retrieval = retrieveCatalogContext(trimmedMessage, historySnapshot)
            val historyToSend = historySnapshot.filter { !it.isPending }
            val response = runCatching {
                remoteDataSource.sendMessage(
                    message = trimmedMessage,
                    history = historyToSend,
                    intent = retrieval.intent.name,
                    catalogContext = retrieval.context,
                )
            }.getOrElse {
                retrieval.fallbackResponse
            }

            _history.value = _history.value.map { msg ->
                if (msg.id == pendingMsgId) {
                    ChatMessage(
                        id = pendingMsgId,
                        role = ChatRole.MODEL,
                        content = response,
                        timestamp = System.currentTimeMillis(),
                        isPending = false,
                        products = retrieval.cards,
                    )
                } else {
                    msg
                }
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            _history.value = _history.value.filter { it.id != pendingMsgId }
            DataResult.Error(DomainError.Unknown(e))
        }
    }

    private suspend fun retrieveCatalogContext(
        userQuery: String,
        historySnapshot: List<ChatMessage>,
    ): CatalogRetrieval {
        val intent = detectIntent(userQuery, historySnapshot)
        return when (intent) {
            ChatShoppingIntent.PRODUCT_SEARCH -> retrieveProductSearch(userQuery)
            ChatShoppingIntent.PRODUCT_COMPARISON -> retrieveProductComparison(userQuery, historySnapshot)
            ChatShoppingIntent.OUTFIT_RECOMMENDATION -> retrieveOutfitRecommendation(userQuery)
            ChatShoppingIntent.GENERAL_HELP -> CatalogRetrieval(
                intent = intent,
                context = "No catalog products were retrieved because the user did not ask for product search, comparison, or outfit recommendation.",
                cards = emptyList(),
                fallbackResponse = "I can help you search WearZone products, compare real items, or build an outfit from the current catalog. Try asking for a category, color, brand, or style.",
            )
        }
    }

    private suspend fun retrieveProductSearch(userQuery: String): CatalogRetrieval {
        val allProducts = loadProductsOrThrow()
        val ranked = rankProductsForQuery(userQuery, allProducts).take(MAX_CHAT_PRODUCTS)
        val cards = ranked.map { (product, reason) -> product.toChatCard(reason) }
        val context = if (cards.isEmpty()) {
            "No real WearZone catalog products matched the query: \"$userQuery\". Do not invent alternatives."
        } else {
            buildProductCardsContext(cards)
        }
        val fallback = if (cards.isEmpty()) {
            "I checked the real WearZone catalog, but I couldn't find products matching \"$userQuery\". No fake items from me — the catalog said no."
        } else {
            buildSearchFallback(cards)
        }
        return CatalogRetrieval(ChatShoppingIntent.PRODUCT_SEARCH, context, cards, fallback)
    }

    private suspend fun retrieveProductComparison(
        userQuery: String,
        historySnapshot: List<ChatMessage>,
    ): CatalogRetrieval {
        val ids = extractRequestedProductIds(userQuery)
            .ifEmpty { recentProductIdsFromHistory(historySnapshot) }
            .distinct()
            .take(MAX_COMPARE_PRODUCTS)

        if (ids.size < 2) {
            return CatalogRetrieval(
                intent = ChatShoppingIntent.PRODUCT_COMPARISON,
                context = "The user asked for a comparison, but fewer than two real product IDs were available. Ask the user to pick two products from shown cards.",
                cards = emptyList(),
                fallbackResponse = "I need two real catalog products to compare. Send two product IDs or ask me to show products first, then say which two you want compared.",
            )
        }

        val summariesById = loadProductsOrNull().associateBy { it.id }
        val details = ids.mapNotNull { id -> loadProductDetailOrNull(id) }

        if (details.size < 2) {
            return CatalogRetrieval(
                intent = ChatShoppingIntent.PRODUCT_COMPARISON,
                context = "The requested product IDs could not be loaded from the real catalog. Do not compare guessed items.",
                cards = emptyList(),
                fallbackResponse = "I couldn't load enough real product details to compare those items. Try opening products from the cards or send two valid product IDs.",
            )
        }

        val cards = details.map { detail ->
            detail.toChatCard(
                summary = summariesById[detail.id],
                reason = "Selected for comparison from the real catalog.",
            )
        }
        val context = buildComparisonContext(details, summariesById)
        return CatalogRetrieval(
            intent = ChatShoppingIntent.PRODUCT_COMPARISON,
            context = context,
            cards = cards,
            fallbackResponse = buildComparisonFallback(details, summariesById),
        )
    }

    private suspend fun retrieveOutfitRecommendation(userQuery: String): CatalogRetrieval {
        val allProducts = loadProductsOrThrow()
        val outfit = buildOutfit(userQuery, allProducts)
        val cards = outfit.selected.map { (slot, product) ->
            product.toChatCard("${slot.displayName}: ${product.bestReasonFor(userQuery)}")
        }
        val context = buildOutfitContext(userQuery, outfit, cards)
        return CatalogRetrieval(
            intent = ChatShoppingIntent.OUTFIT_RECOMMENDATION,
            context = context,
            cards = cards,
            fallbackResponse = buildOutfitFallback(outfit, cards),
        )
    }

    private fun detectIntent(
        message: String,
        historySnapshot: List<ChatMessage>,
    ): ChatShoppingIntent {
        val normalized = message.normalized()
        val hasPreviousProducts = historySnapshot.any { it.products.isNotEmpty() }
        val comparisonTerms = listOf("compare", "comparison", "versus", " vs ", "which is better", "difference between")
        val outfitTerms = listOf("outfit", "look", "style me", "recommend me an outfit", "complete set", "match with", "wear with")
        val productTerms = listOf(
            "show", "find", "search", "product", "products", "catalog", "available", "do you have",
            "t shirt", "shirt", "shoe", "sneaker", "bag", "dress", "hoodie", "jacket", "pants", "jeans", "shorts",
        )
        return when {
            comparisonTerms.any { normalized.contains(it.trim().normalized()) } -> ChatShoppingIntent.PRODUCT_COMPARISON
            normalized.contains("these two") && hasPreviousProducts -> ChatShoppingIntent.PRODUCT_COMPARISON
            outfitTerms.any { normalized.contains(it.normalized()) } -> ChatShoppingIntent.OUTFIT_RECOMMENDATION
            productTerms.any { normalized.contains(it.normalized()) } || extractColors(normalized).isNotEmpty() -> ChatShoppingIntent.PRODUCT_SEARCH
            else -> ChatShoppingIntent.GENERAL_HELP
        }
    }

    private suspend fun loadProductsOrThrow(): List<Product> {
        return when (val result = productRepository.getProducts()) {
            is DataResult.Success -> result.data
            is DataResult.Error -> throw IllegalStateException("Unable to load real WearZone products.")
        }
    }

    private suspend fun loadProductsOrNull(): List<Product> = when (val result = productRepository.getProducts()) {
        is DataResult.Success -> result.data
        is DataResult.Error -> emptyList()
    }

    private suspend fun loadProductDetailOrNull(productId: String): ProductDetail? {
        val id = productId.toLongOrNull() ?: return null
        return when (val result = productRepository.getProductDetail(id)) {
            is DataResult.Success -> result.data
            is DataResult.Error -> null
        }
    }

    private fun rankProductsForQuery(query: String, products: List<Product>): List<Pair<Product, String>> {
        val normalizedQuery = query.normalized()
        val broadQuery = normalizedQuery.isBlank() || BROAD_SEARCH_TERMS.any { normalizedQuery.contains(it) }
        val queryTokens = normalizedQuery.tokens()
        val requestedSlots = inferSlotsFromQuery(normalizedQuery)
        val requestedColors = extractColors(normalizedQuery)

        return products.mapNotNull { product ->
            val blob = product.searchBlob()
            val slot = product.catalogSlot()
            if (requestedSlots.isNotEmpty() && slot !in requestedSlots) return@mapNotNull null
            if (requestedColors.isNotEmpty() && requestedColors.none { blob.contains(it) }) return@mapNotNull null

            var score = 0
            var tokenHits = 0
            if (broadQuery) score += 1
            if (normalizedQuery.isNotBlank() && blob.contains(normalizedQuery)) score += 10
            if (requestedSlots.isNotEmpty() && slot in requestedSlots) score += 8
            requestedColors.forEach { color ->
                if (blob.contains(color)) score += 6
            }
            queryTokens.forEach { token ->
                val tokenVariants = token.variants()
                val hit = tokenVariants.any { blob.contains(it) }
                if (hit) {
                    tokenHits++
                    score += when {
                        product.title.normalized().containsAny(tokenVariants) -> 5
                        product.productType.normalized().containsAny(tokenVariants) -> 4
                        product.vendor.normalized().containsAny(tokenVariants) -> 3
                        else -> 2
                    }
                }
            }

            val categoryOnlyQuery = requestedSlots.isNotEmpty() && queryTokens.all { it.isSlotToken() }
            if (!broadQuery && queryTokens.isNotEmpty() && tokenHits == 0 && !categoryOnlyQuery) return@mapNotNull null
            if (score <= 0) return@mapNotNull null

            product to product.bestReasonFor(query)
        }.sortedWith(
            compareByDescending<Pair<Product, String>> { if (it.first.isOutOfStock) 0 else 1 }
                .thenByDescending { pair ->
                    val blob = pair.first.searchBlob()
                    var score = 0
                    if (requestedSlots.isNotEmpty() && pair.first.catalogSlot() in requestedSlots) score += 8
                    requestedColors.forEach { if (blob.contains(it)) score += 6 }
                    queryTokens.forEach { token -> if (token.variants().any { blob.contains(it) }) score += 3 }
                    score
                }
                .thenBy { it.first.price }
        )
    }

    private fun buildOutfit(query: String, products: List<Product>): OutfitSelection {
        val selected = linkedMapOf<CatalogSlot, Product>()
        val missing = mutableListOf<CatalogSlot>()
        val slots = listOf(CatalogSlot.TOP, CatalogSlot.BOTTOM, CatalogSlot.SHOES)
        val optionalSlots = listOf(CatalogSlot.BAG_ACCESSORY)
        val alreadySelected = mutableSetOf<String>()

        slots.forEach { slot ->
            val best = products
                .filter { it.id !in alreadySelected && it.catalogSlot() == slot }
                .map { product -> product to outfitScore(query, product) }
                .filter { (_, score) -> score >= 0 }
                .sortedWith(compareByDescending<Pair<Product, Int>> { if (it.first.isOutOfStock) 0 else 1 }.thenByDescending { it.second })
                .firstOrNull()
                ?.first
            if (best == null) {
                missing += slot
            } else {
                selected[slot] = best
                alreadySelected += best.id
            }
        }

        optionalSlots.forEach { slot ->
            val best = products
                .filter { it.id !in alreadySelected && it.catalogSlot() == slot }
                .map { product -> product to outfitScore(query, product) }
                .sortedWith(compareByDescending<Pair<Product, Int>> { if (it.first.isOutOfStock) 0 else 1 }.thenByDescending { it.second })
                .firstOrNull()
                ?.first
            if (best != null) {
                selected[slot] = best
                alreadySelected += best.id
            }
        }

        return OutfitSelection(selected = selected, missingRequiredSlots = missing)
    }

    private fun outfitScore(query: String, product: Product): Int {
        val normalizedQuery = query.normalized()
        val tokens = normalizedQuery.tokens()
        val colors = extractColors(normalizedQuery)
        val blob = product.searchBlob()
        var score = if (product.isOutOfStock) -5 else 1
        colors.forEach { color -> if (blob.contains(color)) score += 5 }
        tokens.forEach { token -> if (token.variants().any { blob.contains(it) }) score += 2 }
        if (blob.contains("casual")) score += 1
        if (blob.contains("new arrivals")) score += 1
        return score
    }

    private fun extractRequestedProductIds(message: String): List<String> {
        val bracketIds = Regex("""\[ID:\s*(\d+)]""", RegexOption.IGNORE_CASE)
            .findAll(message)
            .map { it.groupValues[1] }
            .toList()
        if (bracketIds.isNotEmpty()) return bracketIds

        return Regex("""\b\d{5,}\b""")
            .findAll(message)
            .map { it.value }
            .toList()
    }

    private fun recentProductIdsFromHistory(historySnapshot: List<ChatMessage>): List<String> {
        return historySnapshot
            .asReversed()
            .flatMap { it.products }
            .map { it.productId }
            .distinct()
            .take(2)
    }

    private fun buildProductCardsContext(cards: List<ChatProductCard>): String = buildString {
        appendLine("Real WearZone products retrieved before AI response. Use only these products:")
        cards.forEachIndexed { index, card ->
            appendLine(
                "${index + 1}. productId=${card.productId}; title=${card.title}; imageUrl=${card.imageUrl ?: "Unavailable"}; " +
                    "price=${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Unavailable"}; brand/vendor=${card.vendor.ifBlank { "Unavailable" }}; " +
                    "category/productType=${card.productType?.takeIf { it.isNotBlank() } ?: "Unavailable"}; " +
                    "availability=${availabilityText(card.isOutOfStock)}; reason=${card.reason}"
            )
        }
    }

    private fun buildComparisonContext(
        details: List<ProductDetail>,
        summariesById: Map<String, Product>,
    ): String = buildString {
        appendLine("Compare these real WearZone products only:")
        details.forEachIndexed { index, detail ->
            val summary = summariesById[detail.id]
            appendLine("${index + 1}. productId=${detail.id}")
            appendLine("   title=${detail.title}")
            appendLine("   price=${formatPrice(detail.price, detail.currencyCode)}")
            appendLine("   brand/vendor=${detail.vendor.ifBlank { "Unavailable" }}")
            appendLine("   category/productType=${summary?.productType?.takeIf { it.isNotBlank() } ?: "Unavailable"}")
            appendLine("   description=${detail.descriptionHtml.take(420).ifBlank { "Unavailable" }}")
            appendLine("   colors=${detail.availableColors.ifEmpty { listOf("Unavailable") }.joinToString()}")
            appendLine("   sizes=${detail.availableSizes.ifEmpty { listOf("Unavailable") }.joinToString()}")
            appendLine("   variants=${detail.variants.take(10).joinToString(" | ") { it.variantContext() }.ifBlank { "Unavailable" }}")
            appendLine("   availability=${if (detail.isOutOfStock) "Out of stock" else "Available or variant-dependent"}")
        }
    }

    private fun buildOutfitContext(
        query: String,
        outfit: OutfitSelection,
        cards: List<ChatProductCard>,
    ): String = buildString {
        appendLine("User requested an outfit for: $query")
        appendLine("Use only these selected real WearZone products:")
        cards.forEach { card ->
            appendLine(
                "- productId=${card.productId}; title=${card.title}; slot/reason=${card.reason}; " +
                    "price=${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Unavailable"}; " +
                    "brand/vendor=${card.vendor.ifBlank { "Unavailable" }}; category/productType=${card.productType ?: "Unavailable"}; " +
                    "availability=${availabilityText(card.isOutOfStock)}"
            )
        }
        if (outfit.missingRequiredSlots.isNotEmpty()) {
            appendLine("Missing required outfit slots in real catalog: ${outfit.missingRequiredSlots.joinToString { it.displayName }}. Say this clearly. Do not invent missing items.")
        }
    }

    private fun buildSearchFallback(cards: List<ChatProductCard>): String = buildString {
        appendLine("I found these real WearZone products:")
        cards.forEach { card ->
            appendLine("• ${card.title} — ${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Price unavailable"} — ${card.vendor.ifBlank { "Brand unavailable" }}. ${card.reason}")
        }
    }.trim()

    private fun buildComparisonFallback(
        details: List<ProductDetail>,
        summariesById: Map<String, Product>,
    ): String = buildString {
        appendLine("Here is a real catalog comparison — no guessed products:")
        details.forEach { detail ->
            val summary = summariesById[detail.id]
            appendLine("• ${detail.title}: ${formatPrice(detail.price, detail.currencyCode)}")
            appendLine("  Brand: ${detail.vendor.ifBlank { "Unavailable" }}")
            appendLine("  Category: ${summary?.productType?.takeIf { it.isNotBlank() } ?: "Unavailable"}")
            appendLine("  Colors: ${detail.availableColors.ifEmpty { listOf("Unavailable") }.joinToString()}")
            appendLine("  Sizes: ${detail.availableSizes.ifEmpty { listOf("Unavailable") }.joinToString()}")
            appendLine("  Availability: ${if (detail.isOutOfStock) "Out of stock" else "Available or variant-dependent"}")
        }
    }.trim()

    private fun buildOutfitFallback(
        outfit: OutfitSelection,
        cards: List<ChatProductCard>,
    ): String = buildString {
        if (cards.isEmpty()) {
            append("I couldn't build an outfit because no matching real catalog products were available.")
            return@buildString
        }
        appendLine("I built this outfit from real WearZone catalog items:")
        cards.forEach { card ->
            appendLine("• ${card.reason}: ${card.title} — ${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Price unavailable"}")
        }
        if (outfit.missingRequiredSlots.isNotEmpty()) {
            appendLine("Missing from the real catalog: ${outfit.missingRequiredSlots.joinToString { it.displayName }}. I did not invent replacements.")
        }
    }.trim()

    private fun Product.toChatCard(reason: String): ChatProductCard = ChatProductCard(
        productId = id,
        title = title,
        imageUrl = imageUrl,
        price = price,
        currencyCode = currencyCode,
        vendor = vendor,
        productType = productType.takeIf { it.isNotBlank() },
        reason = reason,
        isOutOfStock = isOutOfStock,
    )

    private fun ProductDetail.toChatCard(summary: Product?, reason: String): ChatProductCard = ChatProductCard(
        productId = id,
        title = title,
        imageUrl = images.firstOrNull() ?: summary?.imageUrl,
        price = price,
        currencyCode = currencyCode,
        vendor = vendor,
        productType = summary?.productType?.takeIf { it.isNotBlank() },
        reason = reason,
        isOutOfStock = isOutOfStock,
    )

    private fun Product.bestReasonFor(query: String): String {
        val normalizedQuery = query.normalized()
        val blob = searchBlob()
        val reasons = mutableListOf<String>()
        val slot = catalogSlot()
        if (slot != null && inferSlotsFromQuery(normalizedQuery).contains(slot)) {
            reasons += "matches ${slot.displayName.lowercase(Locale.ROOT)} category"
        }
        extractColors(normalizedQuery).firstOrNull { blob.contains(it) }?.let { reasons += "matches $it color" }
        normalizedQuery.tokens().firstOrNull { token -> token.variants().any { blob.contains(it) } }?.let { reasons += "matches \"$it\"" }
        if (vendor.isNotBlank() && normalizedQuery.contains(vendor.normalized())) reasons += "matches brand/vendor"
        if (isOutOfStock) reasons += "currently out of stock"
        return reasons.distinct().take(2).joinToString(prefix = "Because it ").ifBlank {
            "Because it is available in the real WearZone catalog."
        }
    }

    private fun Product.searchBlob(): String = listOf(
        title,
        vendor,
        productType,
        tags.joinToString(" "),
        size.orEmpty(),
        color.orEmpty(),
    ).joinToString(" ").normalized()

    private fun Product.catalogSlot(): CatalogSlot? {
        val blob = searchBlob()
        return when {
            blob.containsAny(SHOES_KEYWORDS) -> CatalogSlot.SHOES
            blob.containsAny(BAG_ACCESSORY_KEYWORDS) -> CatalogSlot.BAG_ACCESSORY
            blob.containsAny(BOTTOM_KEYWORDS) -> CatalogSlot.BOTTOM
            blob.containsAny(TOP_KEYWORDS) -> CatalogSlot.TOP
            else -> null
        }
    }

    private fun inferSlotsFromQuery(normalizedQuery: String): Set<CatalogSlot> = buildSet {
        if (normalizedQuery.containsAny(TOP_QUERY_KEYWORDS)) add(CatalogSlot.TOP)
        if (normalizedQuery.containsAny(BOTTOM_KEYWORDS)) add(CatalogSlot.BOTTOM)
        if (normalizedQuery.containsAny(SHOES_KEYWORDS)) add(CatalogSlot.SHOES)
        if (normalizedQuery.containsAny(BAG_ACCESSORY_KEYWORDS)) add(CatalogSlot.BAG_ACCESSORY)
    }

    private fun extractColors(normalizedText: String): Set<String> = COLOR_KEYWORDS.filterTo(mutableSetOf()) { color ->
        Regex("""\b${Regex.escape(color)}\b""").containsMatchIn(normalizedText)
    }

    private fun String.normalized(): String = lowercase(Locale.ROOT)
        .replace("&", " and ")
        .replace("t-shirts", "t shirts")
        .replace("t-shirt", "t shirt")
        .replace(Regex("[^a-z0-9]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun String.tokens(): List<String> = split(' ')
        .map { it.trim() }
        .filter { it.length > 1 && it !in STOP_WORDS }
        .distinct()

    private fun String.variants(): Set<String> = buildSet {
        add(this@variants)
        if (endsWith("s") && length > 3) add(dropLast(1))
        if (this@variants == "tee") add("t shirt")
        if (this@variants == "tshirt") add("t shirt")
        if (this@variants == "grey") add("gray")
        if (this@variants == "gray") add("grey")
    }

    private fun String.containsAny(values: Iterable<String>): Boolean = values.any { contains(it) }

    private fun String.isSlotToken(): Boolean = SLOT_QUERY_TOKENS.any { slotToken -> variants().contains(slotToken) || slotToken.contains(this) }

    private fun ProductVariant.variantContext(): String {
        val parts = listOf(
            "id=${id.ifBlank { "Unavailable" }}",
            "title=${title.ifBlank { "Unavailable" }}",
            "price=${formatPrice(price, "EGP")}",
            "size=${size ?: "Unavailable"}",
            "color=${color ?: "Unavailable"}",
            "availableQuantity=$availableQuantity",
        )
        return parts.joinToString(", ")
    }

    private fun formatPrice(price: Double, currencyCode: String): String {
        val cleanPrice = if (price % 1.0 == 0.0) price.toInt().toString() else "%.2f".format(Locale.US, price)
        return "$cleanPrice ${currencyCode.ifBlank { "EGP" }}"
    }

    private fun availabilityText(isOutOfStock: Boolean?): String = when (isOutOfStock) {
        true -> "Out of stock"
        false -> "Available or variant-dependent"
        null -> "Unavailable"
    }

    private data class CatalogRetrieval(
        val intent: ChatShoppingIntent,
        val context: String,
        val cards: List<ChatProductCard>,
        val fallbackResponse: String,
    )

    private data class OutfitSelection(
        val selected: LinkedHashMap<CatalogSlot, Product>,
        val missingRequiredSlots: List<CatalogSlot>,
    )

    private enum class ChatShoppingIntent {
        PRODUCT_SEARCH,
        PRODUCT_COMPARISON,
        OUTFIT_RECOMMENDATION,
        GENERAL_HELP,
    }

    private enum class CatalogSlot(val displayName: String) {
        TOP("Top"),
        BOTTOM("Bottom"),
        SHOES("Shoes"),
        BAG_ACCESSORY("Bag/Accessory"),
    }

    private companion object {
        const val MAX_CHAT_PRODUCTS = 8
        const val MAX_COMPARE_PRODUCTS = 4

        val STOP_WORDS = setOf(
            "show", "me", "find", "search", "for", "the", "a", "an", "and", "or", "to", "with", "of", "in", "on",
            "products", "product", "please", "do", "you", "have", "available", "wearzone", "recommend", "want", "need",
        )
        val BROAD_SEARCH_TERMS = setOf("all", "catalog", "clothes", "clothing", "products", "what do you sell")
        val COLOR_KEYWORDS = setOf(
            "black", "white", "blue", "navy", "red", "green", "gray", "grey", "brown", "beige", "cream",
            "pink", "yellow", "orange", "purple", "gold", "silver", "maroon", "khaki",
        )
        val TOP_KEYWORDS = setOf(
            "t shirt", "shirt", "tee", "polo", "hoodie", "jacket", "coat", "top", "blouse", "sweater", "sweatshirt", "dress",
        )
        val TOP_QUERY_KEYWORDS = TOP_KEYWORDS + setOf("t shirts", "shirts", "tees", "tops", "dresses")
        val BOTTOM_KEYWORDS = setOf("pant", "pants", "jean", "jeans", "trouser", "trousers", "short", "shorts", "skirt", "leggings", "jogger", "sweatpant")
        val SHOES_KEYWORDS = setOf("shoe", "shoes", "sneaker", "sneakers", "trainer", "boot", "boots", "sandal", "sandals", "heel", "heels", "loafer")
        val BAG_ACCESSORY_KEYWORDS = setOf("bag", "bags", "backpack", "purse", "wallet", "belt", "cap", "hat", "accessory", "accessories", "sunglasses")
        val SLOT_QUERY_TOKENS = TOP_QUERY_KEYWORDS + BOTTOM_KEYWORDS + SHOES_KEYWORDS + BAG_ACCESSORY_KEYWORDS
    }
}
