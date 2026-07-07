package com.example.wearzone.data.repository

import android.util.Log
import com.example.wearzone.data.remote.ai.chat.IAiChatRemoteDataSource
import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.ai.chat.model.ChatProductCard
import com.example.wearzone.domain.ai.chat.model.ChatRole
import com.example.wearzone.domain.ai.chat.repository.IAiChatRepository
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.model.ProductVariant
import com.example.wearzone.domain.product.repository.IProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
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

        Log.d(TAG, "SmartChat query=\"$trimmedMessage\"")
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
            Log.d(
                TAG,
                "AI request start intent=${retrieval.intent} cards=${retrieval.cards.size} contextChars=${retrieval.context.length}",
            )
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

            Log.d(TAG, "AI request end intent=${retrieval.intent} responseChars=${response.length}")
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
            Log.d(TAG, "UI state update messageId=$pendingMsgId cards=${retrieval.cards.size} productIds=${retrieval.cards.joinToString { it.productId }}")
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
        val criteria = parseSearchCriteria(userQuery)
        val intent = detectIntent(criteria, historySnapshot)
        return when (intent) {
            ChatShoppingIntent.PRODUCT_SEARCH -> retrieveProductSearch(criteria)
            ChatShoppingIntent.PRODUCT_COMPARISON -> retrieveProductComparison(userQuery, historySnapshot)
            ChatShoppingIntent.OUTFIT_RECOMMENDATION -> retrieveOutfitRecommendation(criteria)
            ChatShoppingIntent.CATALOG_OVERVIEW -> retrieveCatalogOverview(userQuery)
            ChatShoppingIntent.GENERAL_HELP -> CatalogRetrieval(
                intent = intent,
                context = "No catalog products were retrieved because the user did not ask for product search, comparison, or outfit recommendation.",
                cards = emptyList(),
                fallbackResponse = "I can help you search WearZone products, compare real items, or build an outfit from the current catalog. Try asking for a category, color, brand, style, or budget.",
            )
        }
    }

    private suspend fun retrieveProductSearch(criteria: ProductSearchCriteria): CatalogRetrieval {
        val allProducts = withContext(Dispatchers.IO) { loadProductsOrThrow() }
        Log.d(TAG, "intent=PRODUCT_SEARCH criteria=$criteria totalProducts=${allProducts.size}")
        val search = withContext(Dispatchers.Default) { searchCatalog(criteria, allProducts) }
        val exactCards = search.exactMatches.map { it.product.toChatCard(it.reason) }
        val fallbackCards = search.closestAlternatives.map { it.product.toChatCard(it.reason) }
        val cards = (exactCards + fallbackCards).distinctBy { it.productId }.take(MAX_CHAT_PRODUCTS)
        Log.d(
            TAG,
            "search hardFiltered=${search.hardFilteredCount} ranked=${search.rankedCount} exact=${exactCards.size} fallback=${fallbackCards.size} selected=${cards.joinToString { it.productId }}",
        )
        val context = buildSearchContext(criteria, exactCards, fallbackCards, search.noExactReason)
        val fallback = buildSearchFallback(criteria, exactCards, fallbackCards, search.noExactReason)
        return CatalogRetrieval(ChatShoppingIntent.PRODUCT_SEARCH, context, cards, fallback)
    }

    private suspend fun retrieveCatalogOverview(userQuery: String): CatalogRetrieval {
        val allProducts = withContext(Dispatchers.IO) { loadProductsOrThrow() }
        val overview = withContext(Dispatchers.Default) { buildCatalogOverview(userQuery, allProducts) }
        Log.d(TAG, "intent=CATALOG_OVERVIEW totalProducts=${allProducts.size} sampleCards=${overview.cards.size} selected=${overview.cards.joinToString { it.productId }}")
        return CatalogRetrieval(
            intent = ChatShoppingIntent.CATALOG_OVERVIEW,
            context = overview.context,
            cards = overview.cards,
            fallbackResponse = overview.fallbackResponse,
        )
    }

    private suspend fun retrieveProductComparison(
        userQuery: String,
        historySnapshot: List<ChatMessage>,
    ): CatalogRetrieval {
        val allProducts = withContext(Dispatchers.IO) { loadProductsOrThrow() }
        Log.d(TAG, "intent=PRODUCT_COMPARISON query=\"$userQuery\" totalProducts=${allProducts.size}")

        val requestedByTitle = withContext(Dispatchers.Default) {
            resolveComparisonProductsByTitle(userQuery, allProducts)
        }

        val ids = when {
            requestedByTitle.foundProducts.size >= 2 -> requestedByTitle.foundProducts.map { it.id }
            requestedByTitle.requestedNames.isNotEmpty() -> emptyList()
            else -> extractRequestedProductIds(userQuery)
                .ifEmpty { recentProductIdsFromHistory(historySnapshot, userQuery) }
                .distinct()
                .take(MAX_COMPARE_PRODUCTS)
        }

        if (requestedByTitle.requestedNames.isNotEmpty() && requestedByTitle.foundProducts.size < 2) {
            val foundCards = requestedByTitle.foundProducts.map {
                it.toChatCard("Selected by title from the real catalog, but comparison needs another requested product.")
            }
            val missing = requestedByTitle.missingNames.joinToString().ifBlank { "one of the requested products" }
            Log.d(TAG, "comparison titleResolve found=${requestedByTitle.foundProducts.map { it.id }} missing=${requestedByTitle.missingNames}")
            return CatalogRetrieval(
                intent = ChatShoppingIntent.PRODUCT_COMPARISON,
                context = buildComparisonNotFoundContext(userQuery, requestedByTitle),
                cards = foundCards,
                fallbackResponse = "I found ${requestedByTitle.foundProducts.size} requested product(s), but I could not find: $missing. I did not replace it with unrelated products.",
            )
        }

        if (ids.size < 2) {
            return CatalogRetrieval(
                intent = ChatShoppingIntent.PRODUCT_COMPARISON,
                context = "The user asked for a comparison, but fewer than two real product IDs or product titles were resolved. Ask the user to pick two products from shown cards.",
                cards = emptyList(),
                fallbackResponse = "I need two real catalog products to compare. Send two product titles, two product IDs, or ask me to show products first, then say which two you want compared.",
            )
        }

        val summariesById = allProducts.associateBy { it.id }
        val details = withContext(Dispatchers.IO) { ids.take(MAX_COMPARE_PRODUCTS).mapNotNull { id -> loadProductDetailOrNull(id) } }

        if (details.size < 2) {
            return CatalogRetrieval(
                intent = ChatShoppingIntent.PRODUCT_COMPARISON,
                context = "The requested product IDs could not be loaded from the real catalog. Do not compare guessed items.",
                cards = ids.mapNotNull { summariesById[it] }
                    .map { it.toChatCard("Requested for comparison, but full details could not be loaded.") },
                fallbackResponse = "I couldn't load enough real product details to compare those items. I did not compare guessed or replacement products.",
            )
        }

        val cards = details.map { detail ->
            detail.toChatCard(
                summary = summariesById[detail.id],
                reason = "Selected for comparison from the real catalog.",
            )
        }
        val context = buildComparisonContext(details, summariesById)
        Log.d(TAG, "comparison selected=${cards.joinToString { it.productId }} exactTitle=${requestedByTitle.requestedNames.isNotEmpty()}")
        return CatalogRetrieval(
            intent = ChatShoppingIntent.PRODUCT_COMPARISON,
            context = context,
            cards = cards,
            fallbackResponse = buildComparisonFallback(details, summariesById),
        )
    }

    private suspend fun retrieveOutfitRecommendation(criteria: ProductSearchCriteria): CatalogRetrieval {
        val allProducts = withContext(Dispatchers.IO) { loadProductsOrThrow() }
        Log.d(TAG, "intent=OUTFIT_RECOMMENDATION criteria=$criteria totalProducts=${allProducts.size}")
        val outfit = withContext(Dispatchers.Default) { buildOutfit(criteria, allProducts) }
        val cards = outfit.selected.map { selected ->
            selected.product.toChatCard(selected.reason)
        }.take(MAX_CHAT_PRODUCTS)
        Log.d(TAG, "outfit selected=${cards.joinToString { it.productId }} notes=${outfit.notes}")
        val context = buildOutfitContext(criteria, outfit, cards)
        return CatalogRetrieval(
            intent = ChatShoppingIntent.OUTFIT_RECOMMENDATION,
            context = context,
            cards = cards,
            fallbackResponse = buildOutfitFallback(outfit, cards),
        )
    }

    private fun parseSearchCriteria(rawQuery: String): ProductSearchCriteria {
        val normalized = rawQuery.normalizedForSearch()
        val kinds = inferKinds(normalized)
        val colors = COLOR_KEYWORDS.filterTo(mutableSetOf()) { normalized.containsPhrase(it) }
        val materials = MATERIAL_KEYWORDS.filterTo(mutableSetOf()) { normalized.containsPhrase(it) }
        val styleTerms = STYLE_KEYWORDS.filterTo(mutableSetOf()) { normalized.containsPhrase(it) }
        val priceRange = parsePriceRange(normalized)
        val hasKidsTerm = normalized.containsAnyPhrase(KIDS_QUERY_KEYWORDS)
        val hasWomenTerm = normalized.containsAnyPhrase(WOMEN_QUERY_KEYWORDS)
        val hasMenTerm = normalized.containsAnyPhrase(MEN_QUERY_KEYWORDS)
        val gender = when {
            hasKidsTerm && normalized.containsPhrase("kids") -> ProductGender.KIDS
            hasWomenTerm -> ProductGender.WOMEN
            hasMenTerm -> ProductGender.MEN
            hasKidsTerm -> ProductGender.KIDS
            normalized.containsAnyPhrase(UNISEX_KEYWORDS) -> ProductGender.UNISEX
            else -> null
        }
        val terms = normalized.searchTokens()
            .filterNot { token ->
                token in PRODUCT_SEARCH_TERMS ||
                        token in BROAD_SEARCH_TERMS ||
                        token in COLOR_KEYWORDS ||
                        token in MATERIAL_KEYWORDS ||
                        token in STYLE_KEYWORDS ||
                        token in PRICE_STOP_WORDS ||
                        token in MEN_QUERY_KEYWORDS ||
                        token in WOMEN_QUERY_KEYWORDS ||
                        token in KIDS_QUERY_KEYWORDS
            }
        val hasHardSearchConstraint = kinds.isNotEmpty() ||
                colors.isNotEmpty() ||
                materials.isNotEmpty() ||
                gender != null ||
                priceRange.first != null ||
                priceRange.second != null
        val isCatalogOverview = !hasHardSearchConstraint && normalized.containsAnyPhrase(CATALOG_OVERVIEW_TERMS)
        val isBroadSearch = terms.isEmpty() || normalized.containsAnyPhrase(BROAD_SEARCH_TERMS) || isCatalogOverview
        val hasProductSearchSignal = kinds.isNotEmpty() ||
                colors.isNotEmpty() ||
                materials.isNotEmpty() ||
                gender != null ||
                priceRange.first != null ||
                priceRange.second != null ||
                normalized.containsAnyPhrase(PRODUCT_SEARCH_TERMS)

        return ProductSearchCriteria(
            rawQuery = rawQuery,
            normalizedQuery = normalized,
            normalizedTerms = terms,
            gender = gender,
            requestedKinds = kinds,
            colors = colors,
            materials = materials,
            styleTerms = styleTerms,
            minPrice = priceRange.first,
            maxPrice = priceRange.second,
            isBroadSearch = isBroadSearch,
            hasProductSearchSignal = hasProductSearchSignal,
            isCatalogOverview = isCatalogOverview,
        )
    }

    private fun inferKinds(normalized: String): Set<ProductKind> = buildSet {
        if (normalized.containsAnyPhrase(BOOT_KEYWORDS)) add(ProductKind.BOOTS)
        if (normalized.containsAnyPhrase(SNEAKER_KEYWORDS)) add(ProductKind.SNEAKERS)
        if (normalized.containsAnyPhrase(LOAFER_KEYWORDS)) add(ProductKind.LOAFERS)
        if (normalized.containsAnyPhrase(SANDAL_KEYWORDS)) add(ProductKind.SANDALS)
        if (normalized.containsAnyPhrase(HEEL_KEYWORDS)) add(ProductKind.HEELS)
        if (normalized.containsAnyPhrase(SHOES_QUERY_KEYWORDS) && none { it.isShoeLike && it != ProductKind.SHOES }) add(ProductKind.SHOES)
        if (normalized.containsAnyPhrase(DRESS_KEYWORDS)) add(ProductKind.DRESS)
        if (normalized.containsAnyPhrase(T_SHIRT_KEYWORDS)) add(ProductKind.T_SHIRT)
        if (normalized.containsAnyPhrase(SHIRT_QUERY_KEYWORDS)) add(ProductKind.SHIRT)
        if (normalized.containsAnyPhrase(BLOUSE_KEYWORDS)) add(ProductKind.BLOUSE)
        if (normalized.containsAnyPhrase(HOODIE_KEYWORDS)) add(ProductKind.HOODIE)
        if (normalized.containsAnyPhrase(JACKET_KEYWORDS)) add(ProductKind.JACKET)
        if (normalized.containsAnyPhrase(TOP_QUERY_KEYWORDS) && ProductKind.DRESS !in this) add(ProductKind.TOP)
        if (normalized.containsAnyPhrase(JEANS_KEYWORDS)) add(ProductKind.JEANS)
        if (normalized.containsAnyPhrase(PANTS_QUERY_KEYWORDS)) add(ProductKind.PANTS)
        if (normalized.containsAnyPhrase(SHORTS_KEYWORDS)) add(ProductKind.SHORTS)
        if (normalized.containsAnyPhrase(SKIRT_KEYWORDS)) add(ProductKind.SKIRT)
        if (normalized.containsAnyPhrase(BAG_KEYWORDS)) add(ProductKind.BAG)
        if (normalized.containsAnyPhrase(ACCESSORY_QUERY_KEYWORDS)) add(ProductKind.ACCESSORY)
    }

    private fun parsePriceRange(normalized: String): Pair<Double?, Double?> {
        Regex("""between\s+(\d+(?:\.\d+)?)\s+(?:and|to)\s+(\d+(?:\.\d+)?)""").find(normalized)?.let { match ->
            val first = match.groupValues[1].toDoubleOrNull()
            val second = match.groupValues[2].toDoubleOrNull()
            if (first != null && second != null) return minOf(first, second) to maxOf(first, second)
        }

        val max = PRICE_MAX_PATTERNS.firstNotNullOfOrNull { pattern ->
            Regex(pattern).find(normalized)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        }
        val min = PRICE_MIN_PATTERNS.firstNotNullOfOrNull { pattern ->
            Regex(pattern).find(normalized)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        }
        return min to max
    }

    private fun detectIntent(
        criteria: ProductSearchCriteria,
        historySnapshot: List<ChatMessage>,
    ): ChatShoppingIntent {
        val normalized = criteria.normalizedQuery
        val hasPreviousProducts = historySnapshot.any { it.products.isNotEmpty() }
        return when {
            normalized.containsAny(COMPARISON_TERMS) -> ChatShoppingIntent.PRODUCT_COMPARISON
            normalized.contains("these two") && hasPreviousProducts -> ChatShoppingIntent.PRODUCT_COMPARISON
            normalized.contains("first two") && hasPreviousProducts -> ChatShoppingIntent.PRODUCT_COMPARISON
            normalized.containsAny(OUTFIT_TERMS) -> ChatShoppingIntent.OUTFIT_RECOMMENDATION
            criteria.isCatalogOverview -> ChatShoppingIntent.CATALOG_OVERVIEW
            criteria.hasProductSearchSignal -> ChatShoppingIntent.PRODUCT_SEARCH
            else -> ChatShoppingIntent.GENERAL_HELP
        }
    }

    private suspend fun loadProductsOrThrow(): List<Product> {
        return when (val result = productRepository.getProducts()) {
            is DataResult.Success -> result.data.distinctBy { it.id }
            is DataResult.Error -> throw IllegalStateException("Unable to load real WearZone products.")
        }
    }

    private suspend fun loadProductsOrNull(): List<Product> = when (val result = productRepository.getProducts()) {
        is DataResult.Success -> result.data.distinctBy { it.id }
        is DataResult.Error -> emptyList()
    }

    private suspend fun loadProductDetailOrNull(productId: String): ProductDetail? {
        val id = productId.toLongOrNull() ?: return null
        return when (val result = productRepository.getProductDetail(id)) {
            is DataResult.Success -> result.data
            is DataResult.Error -> null
        }
    }

    private fun searchCatalog(criteria: ProductSearchCriteria, products: List<Product>): CatalogSearchResult {
        val priceFiltered = products
            .distinctBy { it.id }
            .filter { product -> criteria.matchesPrice(product.price) }

        val exactGenderFiltered = priceFiltered.filter { product -> criteria.matchesExactGender(product.genderClassification()) }
        val exactCandidates = exactGenderFiltered
            .filter { product -> criteria.matchesExactProductKind(product) }
            .filter { product -> criteria.matchesColors(product) }
            .filter { product -> criteria.matchesMaterials(product) }

        val exactMatches = exactCandidates
            .mapNotNull { product -> product.scoreFor(criteria, MatchBucket.EXACT) }
            .filter { it.score > 0 || criteria.isBroadSearch }
            .sortedWith(productScoreComparator())
            .let { diversifyScoredProducts(it) }
            .take(MAX_CHAT_PRODUCTS)

        if (exactMatches.isNotEmpty()) {
            return CatalogSearchResult(
                exactMatches = exactMatches,
                closestAlternatives = emptyList(),
                noExactReason = null,
                hardFilteredCount = exactCandidates.size,
                rankedCount = exactMatches.size,
            )
        }

        val fallbackCandidates = priceFiltered
            .filter { product -> criteria.matchesFallbackGender(product.genderClassification()) }
            .filter { product -> criteria.matchesColors(product) }
            .filter { product -> criteria.matchesMaterials(product) }
            .filter { product -> criteria.matchesFallbackProductKind(product) }

        val closestAlternatives = fallbackCandidates
            .mapNotNull { product -> product.scoreFor(criteria, MatchBucket.ALTERNATIVE) }
            .filter { it.score > 0 || criteria.isBroadSearch }
            .sortedWith(productScoreComparator())
            .let { diversifyScoredProducts(it) }
            .take(MAX_FALLBACK_PRODUCTS)

        return CatalogSearchResult(
            exactMatches = emptyList(),
            closestAlternatives = closestAlternatives,
            noExactReason = criteria.noExactReason(priceFiltered, exactGenderFiltered),
            hardFilteredCount = exactCandidates.size,
            rankedCount = closestAlternatives.size,
        )
    }

    private fun ProductSearchCriteria.matchesPrice(price: Double): Boolean {
        if (minPrice != null && price < minPrice) return false
        if (maxPrice != null && price > maxPrice) return false
        return true
    }

    private fun ProductSearchCriteria.matchesExactGender(productGender: ProductGender): Boolean {
        val requested = gender ?: return true
        return when (requested) {
            ProductGender.MEN -> productGender == ProductGender.MEN || productGender == ProductGender.UNISEX
            ProductGender.WOMEN -> productGender == ProductGender.WOMEN || productGender == ProductGender.UNISEX
            ProductGender.KIDS -> productGender == ProductGender.KIDS || productGender == ProductGender.UNISEX
            ProductGender.UNISEX -> productGender == ProductGender.UNISEX
            ProductGender.UNKNOWN -> true
        }
    }

    private fun ProductSearchCriteria.matchesFallbackGender(productGender: ProductGender): Boolean {
        val requested = gender ?: return true
        return when (requested) {
            ProductGender.MEN -> productGender == ProductGender.MEN || productGender == ProductGender.UNISEX || productGender == ProductGender.UNKNOWN
            ProductGender.WOMEN -> productGender == ProductGender.WOMEN || productGender == ProductGender.UNISEX || productGender == ProductGender.UNKNOWN
            ProductGender.KIDS -> productGender == ProductGender.KIDS || productGender == ProductGender.UNISEX || productGender == ProductGender.UNKNOWN
            ProductGender.UNISEX -> productGender == ProductGender.UNISEX || productGender == ProductGender.UNKNOWN
            ProductGender.UNKNOWN -> true
        }
    }

    private fun ProductSearchCriteria.matchesExactProductKind(product: Product): Boolean {
        if (requestedKinds.isEmpty()) return true
        val kinds = product.productKinds()
        return requestedKinds.any { requested -> requested.matchesExactly(kinds) }
    }

    private fun ProductSearchCriteria.matchesFallbackProductKind(product: Product): Boolean {
        if (requestedKinds.isEmpty()) return true
        val slot = product.catalogSlot()
        return requestedKinds.any { requested ->
            when (requested) {
                ProductKind.BOOTS,
                ProductKind.SNEAKERS,
                ProductKind.LOAFERS,
                ProductKind.SANDALS,
                ProductKind.HEELS -> slot == CatalogSlot.SHOES
                ProductKind.SHOES -> slot == CatalogSlot.SHOES
                ProductKind.T_SHIRT,
                ProductKind.SHIRT,
                ProductKind.TOP,
                ProductKind.BLOUSE,
                ProductKind.HOODIE,
                ProductKind.JACKET -> slot == CatalogSlot.TOP
                ProductKind.PANTS,
                ProductKind.JEANS,
                ProductKind.TROUSERS,
                ProductKind.SHORTS,
                ProductKind.SKIRT -> slot == CatalogSlot.BOTTOM
                ProductKind.DRESS -> slot == CatalogSlot.DRESS
                ProductKind.BAG -> slot == CatalogSlot.BAG_ACCESSORY
                ProductKind.ACCESSORY -> slot == CatalogSlot.BAG_ACCESSORY
            }
        }
    }

    private fun ProductSearchCriteria.matchesColors(product: Product): Boolean {
        if (colors.isEmpty()) return true
        val blob = product.searchBlob()
        return colors.any { color -> blob.containsPhrase(color) }
    }

    private fun ProductSearchCriteria.matchesMaterials(product: Product): Boolean {
        if (materials.isEmpty()) return true
        val blob = product.searchBlob()
        return materials.any { material -> blob.containsPhrase(material) }
    }

    private fun Product.scoreFor(criteria: ProductSearchCriteria, bucket: MatchBucket): ScoredProduct? {
        val blob = searchBlob()
        val titleText = title.normalizedForSearch()
        val typeText = productType.normalizedForSearch()
        val tagText = tags.joinToString(" ").normalizedForSearch()
        val vendorText = vendor.normalizedForSearch()
        val colorText = color.orEmpty().normalizedForSearch()
        val sizeText = size.orEmpty().normalizedForSearch()
        val kinds = productKinds()
        val productGender = genderClassification()
        val slot = catalogSlot()

        var score = 0
        val reasons = mutableListOf<String>()

        if (!isOutOfStock) score += 12 else score -= 8
        if (criteria.isBroadSearch) score += 5

        val titleMatchScore = titleRelevanceScore(criteria.normalizedQuery, titleText)
        if (titleMatchScore > 0) {
            score += titleMatchScore
            reasons += if (titleMatchScore >= 120) "title matches the requested product name" else "title is highly relevant"
        }

        criteria.gender?.let { requestedGender ->
            when {
                productGender == requestedGender -> {
                    score += 35
                    reasons += "gender matches ${requestedGender.displayName}"
                }
                productGender == ProductGender.UNISEX -> {
                    score += 18
                    reasons += "unisex item fits ${requestedGender.displayName} request"
                }
                productGender == ProductGender.UNKNOWN && bucket == MatchBucket.ALTERNATIVE -> {
                    score += 5
                    reasons += "gender unavailable, shown only as a fallback"
                }
            }
        }

        criteria.requestedKinds.forEach { requested ->
            if (requested.matchesExactly(kinds)) {
                score += when {
                    titleText.containsAny(requested.keywords) -> 55
                    typeText.containsAny(requested.keywords) -> 45
                    tagText.containsAny(requested.keywords) -> 38
                    else -> 28
                }
                reasons += "matches ${requested.displayName}"
            } else if (bucket == MatchBucket.ALTERNATIVE && slot != null && requested.matchesSlot(slot)) {
                score += 14
                reasons += "closest ${slot.displayName.lowercase(Locale.ROOT)} alternative, not exact ${requested.displayName}"
            }
        }

        criteria.colors.forEach { color ->
            if (blob.containsPhrase(color)) {
                score += when {
                    colorText.containsPhrase(color) -> 34
                    titleText.containsPhrase(color) -> 28
                    tagText.containsPhrase(color) -> 24
                    else -> 16
                }
                reasons += "matches $color color"
            } else if (bucket == MatchBucket.ALTERNATIVE) {
                reasons += "requested $color color unavailable on this alternative"
            }
        }

        criteria.materials.forEach { material ->
            if (blob.containsPhrase(material)) {
                score += when {
                    titleText.containsPhrase(material) -> 28
                    typeText.containsPhrase(material) -> 24
                    tagText.containsPhrase(material) -> 20
                    else -> 14
                }
                reasons += "matches $material material"
            } else if (bucket == MatchBucket.ALTERNATIVE) {
                reasons += "requested $material material unavailable on this alternative"
            }
        }

        criteria.normalizedTerms.forEach { token ->
            val variants = token.termVariants()
            val tokenScore = when {
                titleText.containsAny(variants) -> 18
                typeText.containsAny(variants) -> 10
                tagText.containsAny(variants) -> 8
                colorText.containsAny(variants) -> 7
                sizeText.containsAny(variants) -> 5
                vendorText.containsAny(variants) -> 5
                blob.containsAny(variants) -> 3
                else -> 0
            }
            if (tokenScore > 0) score += tokenScore
        }

        criteria.styleTerms.forEach { style ->
            if (blob.containsPhrase(style)) {
                score += 8
                reasons += "fits $style style"
            }
        }

        if (criteria.maxPrice != null) {
            score += ((criteria.maxPrice - price).coerceAtLeast(0.0) / criteria.maxPrice.coerceAtLeast(1.0) * 8).toInt()
            reasons += "within ${formatPrice(criteria.maxPrice, currencyCode)} budget"
        }
        if (criteria.minPrice != null) reasons += "above ${formatPrice(criteria.minPrice, currencyCode)} minimum"

        if (criteria.normalizedQuery.isNotBlank() && blob.contains(criteria.normalizedQuery)) {
            score += 45
            reasons += "contains the full query phrase"
        }

        if (score <= 0 && !criteria.isBroadSearch) return null
        val prefix = if (bucket == MatchBucket.EXACT) "Exact match" else "Closest alternative"
        val reasonText = reasons.distinct().take(3).joinToString(", ").ifBlank {
            if (bucket == MatchBucket.EXACT) "available in the real WearZone catalog" else "nearest real catalog item after exact filters found nothing"
        }
        return ScoredProduct(this, score, "$prefix: $reasonText.")
    }

    private fun productScoreComparator(): Comparator<ScoredProduct> =
        compareByDescending<ScoredProduct> { if (it.product.isOutOfStock) 0 else 1 }
            .thenByDescending { it.score }
            .thenBy { it.product.price }
            .thenBy { it.product.title.normalizedForSearch() }

    private fun diversifyScoredProducts(products: List<ScoredProduct>): List<ScoredProduct> {
        val result = mutableListOf<ScoredProduct>()
        val perType = mutableMapOf<String, Int>()
        val perVendor = mutableMapOf<String, Int>()

        products.forEach { scored ->
            val typeKey = scored.product.productType.normalizedForSearch().ifBlank { scored.product.catalogSlot()?.name.orEmpty() }
            val vendorKey = scored.product.vendor.normalizedForSearch().ifBlank { "unknown" }
            if ((perType[typeKey] ?: 0) < 2 && (perVendor[vendorKey] ?: 0) < 3) {
                result += scored
                perType[typeKey] = (perType[typeKey] ?: 0) + 1
                perVendor[vendorKey] = (perVendor[vendorKey] ?: 0) + 1
            }
        }

        if (result.size < MAX_CHAT_PRODUCTS) {
            products.forEach { scored ->
                if (result.none { it.product.id == scored.product.id }) result += scored
                if (result.size >= MAX_CHAT_PRODUCTS) return@forEach
            }
        }
        return result.distinctBy { it.product.id }
    }

    private fun buildOutfit(criteria: ProductSearchCriteria, products: List<Product>): OutfitSelection {
        val priceFiltered = products.distinctBy { it.id }.filter { criteria.matchesPrice(it.price) }
        val eligible = priceFiltered.filter { product -> criteria.matchesOutfitGender(product.genderClassification(), allowOppositeShoesFallback = false) }
        val selectedIds = mutableSetOf<String>()
        val selected = mutableListOf<OutfitItem>()
        val notes = mutableListOf<String>()

        fun pick(slot: CatalogSlot, required: Boolean, fallbackOppositeGenderForShoes: Boolean = false): OutfitItem? {
            val primaryPool = eligible
                .filter { it.id !in selectedIds }
                .filter { it.catalogSlot() == slot }
            val primary = primaryPool.bestOutfitProduct(criteria, slot, fallback = false)
            if (primary != null) {
                selectedIds += primary.product.id
                return primary
            }

            val unknownPool = priceFiltered
                .filter { it.id !in selectedIds }
                .filter { it.catalogSlot() == slot }
                .filter { product ->
                    val gender = product.genderClassification()
                    gender == ProductGender.UNKNOWN || gender == ProductGender.UNISEX
                }
            val unknownFallback = unknownPool.bestOutfitProduct(criteria, slot, fallback = true)
            if (unknownFallback != null) {
                selectedIds += unknownFallback.product.id
                return unknownFallback
            }

            if (slot == CatalogSlot.SHOES && fallbackOppositeGenderForShoes && criteria.gender != null) {
                val oppositeShoe = priceFiltered
                    .filter { it.id !in selectedIds }
                    .filter { it.catalogSlot() == CatalogSlot.SHOES }
                    .filter { product ->
                        val gender = product.genderClassification()
                        (criteria.gender == ProductGender.WOMEN && gender == ProductGender.MEN) ||
                                (criteria.gender == ProductGender.MEN && gender == ProductGender.WOMEN)
                    }
                    .bestOutfitProduct(criteria, slot, fallback = true, forcedReason = "Fallback shoes: no ${criteria.gender.displayName}/unisex shoes were available; this is opposite-gender stock.")
                if (oppositeShoe != null) {
                    selectedIds += oppositeShoe.product.id
                    return oppositeShoe
                }
            }

            if (required) notes += "${slot.displayName} unavailable in the real catalog for this outfit."
            return null
        }

        val gender = criteria.gender
        val womenRequested = gender == ProductGender.WOMEN
        val menRequested = gender == ProductGender.MEN

        if (womenRequested) {
            val dressOption = buildOutfitOption(criteria, eligible, listOf(CatalogSlot.DRESS, CatalogSlot.SHOES), listOf(CatalogSlot.BAG_ACCESSORY))
            val separatesOption = buildOutfitOption(criteria, eligible, listOf(CatalogSlot.TOP, CatalogSlot.BOTTOM, CatalogSlot.SHOES), listOf(CatalogSlot.BAG_ACCESSORY))
            val useDress = dressOption.requiredFound >= 2 && dressOption.totalScore >= separatesOption.totalScore
            if (useDress) {
                pick(CatalogSlot.DRESS, required = true)?.let(selected::add)
                pick(CatalogSlot.SHOES, required = true, fallbackOppositeGenderForShoes = true)?.let(selected::add)
                pick(CatalogSlot.BAG_ACCESSORY, required = false)?.let(selected::add)
            } else {
                pick(CatalogSlot.TOP, required = true)?.let(selected::add)
                pick(CatalogSlot.BOTTOM, required = true)?.let(selected::add)
                pick(CatalogSlot.SHOES, required = true, fallbackOppositeGenderForShoes = true)?.let(selected::add)
                pick(CatalogSlot.BAG_ACCESSORY, required = false)?.let(selected::add)
            }
        } else {
            pick(CatalogSlot.TOP, required = true)?.let(selected::add)
            pick(CatalogSlot.BOTTOM, required = true)?.let(selected::add)
            pick(CatalogSlot.SHOES, required = true, fallbackOppositeGenderForShoes = !menRequested)?.let(selected::add)
            pick(CatalogSlot.BAG_ACCESSORY, required = false)?.let(selected::add)
        }

        if (selected.none { it.slot == CatalogSlot.BAG_ACCESSORY }) {
            notes += "Bag/accessory is unavailable or not compatible in the real catalog."
        }

        return OutfitSelection(
            selected = selected.distinctBy { it.product.id },
            notes = notes.distinct(),
        )
    }

    private fun ProductSearchCriteria.matchesOutfitGender(
        productGender: ProductGender,
        allowOppositeShoesFallback: Boolean,
    ): Boolean {
        val requested = gender ?: return true
        return when (requested) {
            ProductGender.WOMEN -> productGender == ProductGender.WOMEN || productGender == ProductGender.UNISEX || allowOppositeShoesFallback
            ProductGender.MEN -> productGender == ProductGender.MEN || productGender == ProductGender.UNISEX || allowOppositeShoesFallback
            ProductGender.KIDS -> productGender == ProductGender.KIDS || productGender == ProductGender.UNISEX
            ProductGender.UNISEX -> productGender == ProductGender.UNISEX
            ProductGender.UNKNOWN -> true
        }
    }

    private fun buildOutfitOption(
        criteria: ProductSearchCriteria,
        products: List<Product>,
        requiredSlots: List<CatalogSlot>,
        optionalSlots: List<CatalogSlot>,
    ): OutfitOptionScore {
        val selectedIds = mutableSetOf<String>()
        var requiredFound = 0
        var totalScore = 0
        (requiredSlots + optionalSlots).forEach { slot ->
            val best = products
                .filter { it.id !in selectedIds && it.catalogSlot() == slot }
                .bestOutfitProduct(criteria, slot, fallback = false)
            if (best != null) {
                selectedIds += best.product.id
                totalScore += best.score
                if (slot in requiredSlots) requiredFound++
            }
        }
        return OutfitOptionScore(requiredFound, totalScore)
    }

    private fun List<Product>.bestOutfitProduct(
        criteria: ProductSearchCriteria,
        slot: CatalogSlot,
        fallback: Boolean,
        forcedReason: String? = null,
    ): OutfitItem? = map { product ->
        val score = outfitScore(criteria, product, slot)
        val prefix = if (fallback) "Fallback ${slot.displayName.lowercase(Locale.ROOT)}" else slot.displayName
        val reason = forcedReason ?: "$prefix: ${product.bestOutfitReason(criteria)}"
        OutfitItem(slot, product, score, reason)
    }
        .sortedWith(compareByDescending<OutfitItem> { if (it.product.isOutOfStock) 0 else 1 }.thenByDescending { it.score }.thenBy { it.product.price })
        .firstOrNull()

    private fun outfitScore(criteria: ProductSearchCriteria, product: Product, slot: CatalogSlot): Int {
        val blob = product.searchBlob()
        var score = if (product.isOutOfStock) -10 else 10
        if (product.catalogSlot() == slot) score += 35
        criteria.gender?.let { requested ->
            when (product.genderClassification()) {
                requested -> score += 30
                ProductGender.UNISEX -> score += 14
                ProductGender.UNKNOWN -> score += 3
                else -> score -= 30
            }
        }
        criteria.colors.forEach { color -> if (blob.containsPhrase(color)) score += 12 }
        criteria.materials.forEach { material -> if (blob.containsPhrase(material)) score += 8 }
        criteria.styleTerms.forEach { style -> if (blob.containsPhrase(style)) score += 12 }
        if (criteria.styleTerms.isEmpty() && blob.containsPhrase("casual")) score += 3
        criteria.normalizedTerms.forEach { term -> if (blob.containsAny(term.termVariants())) score += 2 }
        return score
    }

    private fun Product.bestOutfitReason(criteria: ProductSearchCriteria): String {
        val parts = mutableListOf<String>()
        criteria.gender?.let { requested ->
            when (genderClassification()) {
                requested -> parts += "matches ${requested.displayName}"
                ProductGender.UNISEX -> parts += "unisex fit for ${requested.displayName}"
                ProductGender.UNKNOWN -> parts += "gender unavailable, used as fallback"
                else -> parts += "gender fallback"
            }
        }
        criteria.styleTerms.firstOrNull { searchBlob().containsPhrase(it) }?.let { parts += "fits $it style" }
        criteria.colors.firstOrNull { searchBlob().containsPhrase(it) }?.let { parts += "matches $it color" }
        if (!isOutOfStock) parts += "available or variant-dependent"
        return parts.distinct().take(3).joinToString(", ").ifBlank { "selected from real WearZone catalog" }
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

    private fun recentProductIdsFromHistory(historySnapshot: List<ChatMessage>, userQuery: String): List<String> {
        val recentCards = historySnapshot
            .asReversed()
            .firstOrNull { it.products.isNotEmpty() }
            ?.products
            .orEmpty()

        val normalized = userQuery.normalizedForSearch()
        val ordinals = extractOrdinals(normalized)
        if (ordinals.isNotEmpty()) {
            return ordinals.mapNotNull { ordinal -> recentCards.getOrNull(ordinal - 1)?.productId }
        }

        return recentCards.map { it.productId }.take(2)
    }

    private fun extractOrdinals(normalized: String): List<Int> = buildList {
        if (normalized.contains("first two") || normalized.contains("these two")) {
            add(1)
            add(2)
            return@buildList
        }
        ORDINAL_TERMS.forEach { (term, index) ->
            if (normalized.containsPhrase(term)) add(index)
        }
    }.distinct()


    private fun resolveComparisonProductsByTitle(
        userQuery: String,
        products: List<Product>,
    ): ComparisonTitleResolution {
        val normalizedQuery = userQuery.normalizedForSearch()
        val exactTitleMatches = products
            .filter { product -> normalizedQuery.containsPhrase(product.title.normalizedForSearch()) }
            .sortedByDescending { it.title.normalizedForSearch().length }
            .distinctBy { it.id }
            .take(MAX_COMPARE_PRODUCTS)

        if (exactTitleMatches.size >= 2) {
            return ComparisonTitleResolution(
                requestedNames = exactTitleMatches.map { it.title },
                foundProducts = exactTitleMatches,
                missingNames = emptyList(),
            )
        }

        val requestedNames = extractComparisonNameChunks(normalizedQuery)
        if (requestedNames.isEmpty()) {
            return ComparisonTitleResolution(emptyList(), exactTitleMatches, emptyList())
        }

        val usedIds = mutableSetOf<String>()
        val found = mutableListOf<Product>()
        val missing = mutableListOf<String>()
        requestedNames.forEach { requestedName ->
            val best = products
                .filter { it.id !in usedIds }
                .map { product -> product to fuzzyTitleScore(requestedName, product.title.normalizedForSearch()) }
                .filter { (_, score) -> score >= FUZZY_TITLE_MIN_SCORE }
                .maxWithOrNull(compareBy<Pair<Product, Int>> { it.second }.thenByDescending { titleTokenOverlap(requestedName, it.first.title.normalizedForSearch()) })
                ?.first
            if (best != null) {
                usedIds += best.id
                found += best
            } else {
                missing += requestedName
            }
        }

        return ComparisonTitleResolution(
            requestedNames = requestedNames,
            foundProducts = found.distinctBy { it.id }.take(MAX_COMPARE_PRODUCTS),
            missingNames = missing,
        )
    }

    private fun extractComparisonNameChunks(normalizedQuery: String): List<String> {
        var value = normalizedQuery
        COMPARISON_NOISE_TERMS.forEach { term ->
            val phrase = Regex.escape(term.normalizedForSearch())
            value = Regex("(^|\\s)$phrase($|\\s)").replace(value) { match ->
                val prefix = if (match.value.startsWith(" ")) " " else ""
                val suffix = if (match.value.endsWith(" ")) " " else ""
                "$prefix|$suffix"
            }
        }
        return value
            .split("|")
            .map { chunk ->
                chunk.searchTokens()
                    .filterNot { it in COMPARISON_EXTRA_STOP_WORDS }
                    .joinToString(" ")
            }
            .filter { it.length >= 4 }
            .filterNot { it.containsAnyPhrase(CATALOG_OVERVIEW_TERMS) }
            .distinct()
            .take(MAX_COMPARE_PRODUCTS)
    }

    private fun fuzzyTitleScore(requestedName: String, title: String): Int {
        if (requestedName.isBlank() || title.isBlank()) return 0
        if (requestedName == title) return 160
        if (title.containsPhrase(requestedName) || requestedName.containsPhrase(title)) return 130
        val overlap = titleTokenOverlap(requestedName, title)
        val requestedTokenCount = requestedName.searchTokens().size.coerceAtLeast(1)
        val titleTokenCount = title.searchTokens().size.coerceAtLeast(1)
        val coverage = (overlap * 100) / requestedTokenCount
        val titleCoverage = (overlap * 100) / titleTokenCount
        return coverage + (titleCoverage / 2)
    }

    private fun titleTokenOverlap(left: String, right: String): Int {
        val rightTokens = right.searchTokens().flatMap { it.termVariants() }.toSet()
        return left.searchTokens().count { token -> token.termVariants().any { it in rightTokens } }
    }

    private fun titleRelevanceScore(query: String, title: String): Int {
        if (query.isBlank() || title.isBlank()) return 0
        return when {
            query == title -> 160
            query.containsPhrase(title) -> 140
            title.containsPhrase(query) -> 120
            else -> {
                val score = fuzzyTitleScore(query, title)
                if (score >= 95) score / 2 else 0
            }
        }
    }

    private fun buildCatalogOverview(
        userQuery: String,
        products: List<Product>,
    ): CatalogOverview {
        val uniqueProducts = products.distinctBy { it.id }
        val bySlot = uniqueProducts.groupBy { product -> product.catalogSlot()?.displayName ?: product.productType.takeIf { it.isNotBlank() } ?: "Other" }
        val byProductType = uniqueProducts
            .groupBy { product -> product.productType.takeIf { it.isNotBlank() } ?: product.catalogSlot()?.displayName ?: "Other" }
            .toList()
            .sortedWith(compareByDescending<Pair<String, List<Product>>> { it.second.size }.thenBy { it.first })
        val samples = bySlot.values
            .mapNotNull { slotProducts ->
                slotProducts
                    .sortedWith(productSampleComparator())
                    .firstOrNull()
            }
            .plus(uniqueProducts.sortedWith(productSampleComparator()))
            .distinctBy { it.id }
            .take(MAX_CHAT_PRODUCTS)
            .map { product ->
                product.toChatCard("Sample from ${product.catalogSlot()?.displayName ?: product.productType.ifBlank { "real catalog" }}.")
            }

        val context = buildString {
            appendLine("Real WearZone full catalog overview for query: $userQuery")
            appendLine("Total real products loaded: ${uniqueProducts.size}")
            appendLine("Product types/categories:")
            byProductType.take(16).forEach { (type, items) -> appendLine("- $type: ${items.size}") }
            appendLine("Samples below are diverse examples, not the whole catalog:")
            appendProductCards(samples)
            if (uniqueProducts.size > samples.size) {
                appendLine("Tell the user these are samples only; more real products exist in the catalog.")
            }
        }
        val fallback = buildString {
            appendLine("WearZone has ${uniqueProducts.size} real catalog products loaded.")
            if (byProductType.isNotEmpty()) {
                appendLine("Main product groups: ${byProductType.take(8).joinToString { "${it.first} (${it.second.size})" }}.")
            }
            appendLine("The cards below are only samples from different categories, not the entire catalog.")
        }.trim()
        return CatalogOverview(context = context, cards = samples, fallbackResponse = fallback)
    }

    private fun productSampleComparator(): Comparator<Product> =
        compareByDescending<Product> { if (it.isOutOfStock) 0 else 1 }
            .thenBy { it.catalogSlot()?.ordinal ?: Int.MAX_VALUE }
            .thenBy { it.productType.normalizedForSearch() }
            .thenBy { it.price }
            .thenBy { it.title.normalizedForSearch() }

    private fun buildComparisonNotFoundContext(
        userQuery: String,
        resolution: ComparisonTitleResolution,
    ): String = buildString {
        appendLine("The user asked to compare by product title: $userQuery")
        appendLine("Found real requested products: ${resolution.foundProducts.joinToString { it.title }.ifBlank { "none" }}")
        appendLine("Missing requested products: ${resolution.missingNames.joinToString().ifBlank { "unresolved requested product" }}")
        appendLine("Do not replace missing products with unrelated products. Do not show women's alternatives for requested men's titles.")
    }

    private fun buildSearchContext(
        criteria: ProductSearchCriteria,
        exactCards: List<ChatProductCard>,
        fallbackCards: List<ChatProductCard>,
        noExactReason: String?,
    ): String = buildString {
        appendLine("Real WearZone catalog retrieval result for query: ${criteria.rawQuery}")
        appendLine("Hard filters applied before ranking:")
        appendLine("- price: ${criteria.priceFilterText()}")
        appendLine("- gender: ${criteria.gender?.displayName ?: "not specified"}")
        appendLine("- product type/category: ${criteria.requestedKinds.joinToString { it.displayName }.ifBlank { "not specified" }}")
        appendLine("- colors: ${criteria.colors.joinToString().ifBlank { "not specified" }}")
        appendLine("- materials: ${criteria.materials.joinToString().ifBlank { "not specified" }}")
        if (exactCards.isNotEmpty()) {
            appendLine("\nExact matches:")
            appendProductCards(exactCards)
            appendLine("Use the heading 'Exact matches' in the response. Do not call fallback products exact matches.")
        } else {
            appendLine("\nNo exact matches found.")
            appendLine(noExactReason ?: "No real catalog product satisfied all hard filters.")
            if (fallbackCards.isNotEmpty()) {
                appendLine("\nClosest alternatives:")
                appendProductCards(fallbackCards)
                appendLine("Use the heading 'Closest alternatives' and clearly state why these are not exact matches.")
            } else {
                appendLine("No closest alternatives are allowed without violating the hard filters. Do not invent products.")
            }
        }
    }

    private fun StringBuilder.appendProductCards(cards: List<ChatProductCard>) {
        cards.take(MAX_CHAT_PRODUCTS).forEachIndexed { index, card ->
            appendLine(
                "${index + 1}. ${card.title}; " +
                        "price=${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Unavailable"}; " +
                        "brand/vendor=${card.vendor.ifBlank { "Unavailable" }}; category/productType=${card.productType?.takeIf { it.isNotBlank() } ?: "Unavailable"}; " +
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
            appendLine("${index + 1}. title=${detail.title}")
            appendLine("   price=${formatPrice(detail.price, detail.currencyCode)}")
            appendLine("   brand/vendor=${detail.vendor.ifBlank { "Unavailable" }}")
            appendLine("   category/productType=${summary?.productType?.takeIf { it.isNotBlank() } ?: "Unavailable"}")
            appendLine("   description=${detail.descriptionHtml.take(180).ifBlank { "Unavailable" }}")
            appendLine("   colors=${detail.availableColors.ifEmpty { listOf("Unavailable") }.joinToString()}")
            appendLine("   sizes=${detail.availableSizes.ifEmpty { listOf("Unavailable") }.joinToString()}")
            appendLine("   variants=${detail.variants.take(5).joinToString(" | ") { it.variantContext() }.ifBlank { "Unavailable" }}")
            appendLine("   availability=${if (detail.isOutOfStock) "Out of stock" else "Available or variant-dependent"}")
        }
    }

    private fun buildOutfitContext(
        criteria: ProductSearchCriteria,
        outfit: OutfitSelection,
        cards: List<ChatProductCard>,
    ): String = buildString {
        appendLine("User requested an outfit for: ${criteria.rawQuery}")
        appendLine("Outfit rules already applied deterministically before AI response.")
        appendLine("Use only these selected real WearZone products:")
        cards.forEach { card ->
            appendLine(
                "- title=${card.title}; slot/reason=${card.reason}; " +
                        "price=${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Unavailable"}; " +
                        "brand/vendor=${card.vendor.ifBlank { "Unavailable" }}; category/productType=${card.productType ?: "Unavailable"}; " +
                        "availability=${availabilityText(card.isOutOfStock)}"
            )
        }
        if (outfit.notes.isNotEmpty()) {
            appendLine("Catalog limitations to say clearly: ${outfit.notes.joinToString(" ")}")
        }
        if (cards.isEmpty()) appendLine("No outfit products were found. Do not invent missing items.")
    }

    private fun buildSearchFallback(
        criteria: ProductSearchCriteria,
        exactCards: List<ChatProductCard>,
        fallbackCards: List<ChatProductCard>,
        noExactReason: String?,
    ): String = buildString {
        if (exactCards.isNotEmpty()) {
            appendLine("Exact matches from the real WearZone catalog:")
            exactCards.forEach { card ->
                appendLine("• ${card.title} — ${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Price unavailable"} — ${card.vendor.ifBlank { "Brand unavailable" }}. ${card.reason}")
            }
            return@buildString
        }

        appendLine("No exact matches found for \"${criteria.rawQuery}\".")
        appendLine(noExactReason ?: "No real catalog product satisfied all hard filters.")
        if (fallbackCards.isNotEmpty()) {
            appendLine("Closest alternatives that still respect hard filters:")
            fallbackCards.forEach { card ->
                appendLine("• ${card.title} — ${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Price unavailable"} — ${card.vendor.ifBlank { "Brand unavailable" }}. ${card.reason}")
            }
        } else {
            appendLine("I did not show alternatives because that would violate the hard filters or require fake products.")
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
            appendLine("  Description: ${detail.descriptionHtml.take(160).ifBlank { "Unavailable" }}")
            appendLine("  Colors: ${detail.availableColors.ifEmpty { listOf("Unavailable") }.joinToString()}")
            appendLine("  Sizes: ${detail.availableSizes.ifEmpty { listOf("Unavailable") }.joinToString()}")
            appendLine("  Variants: ${detail.variants.take(5).joinToString(" | ") { it.variantContext() }.ifBlank { "Unavailable" }}")
            appendLine("  Availability: ${if (detail.isOutOfStock) "Out of stock" else "Available or variant-dependent"}")
        }
    }.trim()

    private fun buildOutfitFallback(
        outfit: OutfitSelection,
        cards: List<ChatProductCard>,
    ): String = buildString {
        if (cards.isEmpty()) {
            append("I couldn't build an outfit because no compatible real catalog products were available. I did not invent replacements.")
            return@buildString
        }
        appendLine("Outfit from real WearZone catalog items:")
        cards.forEach { card ->
            appendLine("• ${card.reason}: ${card.title} — ${card.price?.let { formatPrice(it, card.currencyCode) } ?: "Price unavailable"}")
        }
        if (outfit.notes.isNotEmpty()) {
            appendLine(outfit.notes.joinToString(" "))
        }
    }.trim()

    private fun Product.toChatCard(reason: String): ChatProductCard = ChatProductCard(
        productId = id,
        title = title,
        imageUrl = imageUrl,
        price = price,
        currencyCode = currencyCode,
        vendor = vendor,
        productType = productType.takeIf { it.isNotBlank() } ?: catalogSlot()?.displayName,
        reason = safeMatchReason(reason),
        isOutOfStock = isOutOfStock,
    )

    private fun ProductDetail.toChatCard(summary: Product?, reason: String): ChatProductCard = ChatProductCard(
        productId = id,
        title = title,
        imageUrl = images.firstOrNull() ?: summary?.imageUrl,
        price = price,
        currencyCode = currencyCode,
        vendor = vendor,
        productType = summary?.productType?.takeIf { it.isNotBlank() } ?: summary?.catalogSlot()?.displayName,
        reason = summary?.safeMatchReason(reason) ?: reason,
        isOutOfStock = isOutOfStock,
    )

    private fun Product.safeMatchReason(reason: String): String {
        val lower = reason.lowercase(Locale.ROOT)
        val slot = catalogSlot()
        val invalidBagReason = lower.contains("bag/accessory") && slot != CatalogSlot.BAG_ACCESSORY
        val invalidShoesReason = lower.contains("shoes") && slot != CatalogSlot.SHOES
        val invalidDressReason = lower.contains("dress") && slot != CatalogSlot.DRESS
        val invalidTopReason = lower.contains("top") && slot == CatalogSlot.DRESS
        return if (invalidBagReason || invalidShoesReason || invalidDressReason || invalidTopReason) {
            "Because it is available in the real catalog."
        } else {
            reason.ifBlank { "Because it is available in the real catalog." }
        }
    }

    private fun Product.searchBlob(): String = listOf(
        title,
        vendor,
        productType,
        tags.joinToString(" "),
        size.orEmpty(),
        color.orEmpty(),
    ).joinToString(" ").normalizedForSearch()

    private fun Product.genderClassification(): ProductGender {
        val blob = searchBlob()
        val hasUnisex = blob.containsAnyPhrase(UNISEX_KEYWORDS)
        val hasWomen = blob.containsAnyPhrase(WOMEN_KEYWORDS)
        val hasMen = blob.containsAnyPhrase(MEN_KEYWORDS)
        val hasKids = blob.containsAnyPhrase(KIDS_KEYWORDS)
        return when {
            hasUnisex || (hasMen && hasWomen) -> ProductGender.UNISEX
            hasWomen -> ProductGender.WOMEN
            hasMen -> ProductGender.MEN
            hasKids -> ProductGender.KIDS
            else -> ProductGender.UNKNOWN
        }
    }

    private fun Product.productKinds(): Set<ProductKind> {
        val blob = searchBlob()
        return buildSet {
            if (blob.containsAnyPhrase(BOOT_KEYWORDS)) {
                add(ProductKind.BOOTS)
                add(ProductKind.SHOES)
            }
            if (blob.containsAnyPhrase(SNEAKER_KEYWORDS)) {
                add(ProductKind.SNEAKERS)
                add(ProductKind.SHOES)
            }
            if (blob.containsAnyPhrase(LOAFER_KEYWORDS)) {
                add(ProductKind.LOAFERS)
                add(ProductKind.SHOES)
            }
            if (blob.containsAnyPhrase(SANDAL_KEYWORDS)) {
                add(ProductKind.SANDALS)
                add(ProductKind.SHOES)
            }
            if (blob.containsAnyPhrase(HEEL_KEYWORDS)) {
                add(ProductKind.HEELS)
                add(ProductKind.SHOES)
            }
            if (blob.containsAnyPhrase(SHOES_KEYWORDS)) add(ProductKind.SHOES)
            if (blob.containsAnyPhrase(DRESS_KEYWORDS)) add(ProductKind.DRESS)
            if (blob.containsAnyPhrase(T_SHIRT_KEYWORDS)) {
                add(ProductKind.T_SHIRT)
                add(ProductKind.TOP)
            }
            if (blob.containsAnyPhrase(SHIRT_KEYWORDS)) {
                add(ProductKind.SHIRT)
                add(ProductKind.TOP)
            }
            if (blob.containsAnyPhrase(BLOUSE_KEYWORDS)) {
                add(ProductKind.BLOUSE)
                add(ProductKind.TOP)
            }
            if (blob.containsAnyPhrase(HOODIE_KEYWORDS)) {
                add(ProductKind.HOODIE)
                add(ProductKind.TOP)
            }
            if (blob.containsAnyPhrase(JACKET_KEYWORDS)) {
                add(ProductKind.JACKET)
                add(ProductKind.TOP)
            }
            if (blob.containsAnyPhrase(TOP_KEYWORDS) && ProductKind.DRESS !in this) add(ProductKind.TOP)
            if (blob.containsAnyPhrase(PANTS_KEYWORDS)) {
                add(ProductKind.PANTS)
                add(ProductKind.TROUSERS)
            }
            if (blob.containsAnyPhrase(JEANS_KEYWORDS)) {
                add(ProductKind.JEANS)
                add(ProductKind.PANTS)
                add(ProductKind.TROUSERS)
            }
            if (blob.containsAnyPhrase(SHORTS_KEYWORDS)) add(ProductKind.SHORTS)
            if (blob.containsAnyPhrase(SKIRT_KEYWORDS)) add(ProductKind.SKIRT)
            if (blob.containsAnyPhrase(BAG_KEYWORDS)) add(ProductKind.BAG)
            if (blob.containsAnyPhrase(ACCESSORY_KEYWORDS)) add(ProductKind.ACCESSORY)
        }
    }

    private fun Product.catalogSlot(): CatalogSlot? {
        val kinds = productKinds()
        return when {
            kinds.any { it.isShoeLike } -> CatalogSlot.SHOES
            ProductKind.BAG in kinds || ProductKind.ACCESSORY in kinds -> CatalogSlot.BAG_ACCESSORY
            ProductKind.DRESS in kinds -> CatalogSlot.DRESS
            kinds.any { it.isBottomLike } -> CatalogSlot.BOTTOM
            kinds.any { it.isTopLike } -> CatalogSlot.TOP
            else -> null
        }
    }

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

    private fun ProductSearchCriteria.noExactReason(
        priceFiltered: List<Product>,
        exactGenderFiltered: List<Product>,
    ): String = buildString {
        val hardFilters = mutableListOf<String>()
        if (maxPrice != null) hardFilters += "price must be <= ${formatPrice(maxPrice, "EGP")}"
        if (minPrice != null) hardFilters += "price must be >= ${formatPrice(minPrice, "EGP")}"
        gender?.let { hardFilters += "gender must be ${it.displayName} or unisex for exact matches" }
        if (requestedKinds.isNotEmpty()) hardFilters += "product type must be ${requestedKinds.joinToString { it.displayName }}"
        if (colors.isNotEmpty()) hardFilters += "color must include ${colors.joinToString()}"
        if (materials.isNotEmpty()) hardFilters += "material must include ${materials.joinToString()}"

        when {
            priceFiltered.isEmpty() && (minPrice != null || maxPrice != null) -> append("No real products satisfy the requested price filter.")
            exactGenderFiltered.isEmpty() && gender != null -> append("No real products satisfy the requested gender filter after price filtering.")
            else -> append("No real products satisfy all exact filters together.")
        }
        if (hardFilters.isNotEmpty()) append(" Filters: ${hardFilters.joinToString("; ")}.")
    }

    private fun ProductSearchCriteria.priceFilterText(): String = when {
        minPrice != null && maxPrice != null -> "${formatPrice(minPrice, "EGP")} to ${formatPrice(maxPrice, "EGP")}"
        maxPrice != null -> "<= ${formatPrice(maxPrice, "EGP")}"
        minPrice != null -> ">= ${formatPrice(minPrice, "EGP")}"
        else -> "not specified"
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

    private fun ProductKind.matchesExactly(productKinds: Set<ProductKind>): Boolean = when (this) {
        ProductKind.SHOES -> productKinds.any { it.isShoeLike }
        ProductKind.T_SHIRT -> ProductKind.T_SHIRT in productKinds || ProductKind.SHIRT in productKinds || ProductKind.TOP in productKinds
        ProductKind.SHIRT -> ProductKind.SHIRT in productKinds || ProductKind.T_SHIRT in productKinds || ProductKind.TOP in productKinds
        ProductKind.TOP -> ProductKind.TOP in productKinds && ProductKind.DRESS !in productKinds
        ProductKind.PANTS,
        ProductKind.TROUSERS -> productKinds.any { it == ProductKind.PANTS || it == ProductKind.TROUSERS || it == ProductKind.JEANS }
        ProductKind.BAG -> ProductKind.BAG in productKinds
        ProductKind.ACCESSORY -> ProductKind.ACCESSORY in productKinds || ProductKind.BAG in productKinds
        else -> this in productKinds
    }

    private fun ProductKind.matchesSlot(slot: CatalogSlot): Boolean = when (slot) {
        CatalogSlot.SHOES -> isShoeLike
        CatalogSlot.TOP -> isTopLike
        CatalogSlot.BOTTOM -> isBottomLike
        CatalogSlot.DRESS -> this == ProductKind.DRESS
        CatalogSlot.BAG_ACCESSORY -> this == ProductKind.BAG || this == ProductKind.ACCESSORY
    }

    private val ProductKind.isShoeLike: Boolean
        get() = this in setOf(ProductKind.SHOES, ProductKind.SNEAKERS, ProductKind.BOOTS, ProductKind.LOAFERS, ProductKind.SANDALS, ProductKind.HEELS)

    private val ProductKind.isTopLike: Boolean
        get() = this in setOf(ProductKind.TOP, ProductKind.T_SHIRT, ProductKind.SHIRT, ProductKind.BLOUSE, ProductKind.HOODIE, ProductKind.JACKET)

    private val ProductKind.isBottomLike: Boolean
        get() = this in setOf(ProductKind.PANTS, ProductKind.JEANS, ProductKind.TROUSERS, ProductKind.SHORTS, ProductKind.SKIRT)

    private fun String.normalizedForSearch(): String {
        var value = lowercase(Locale.ROOT)
            .replace("’s", "")
            .replace("'s", "")
            .replace("-", " ")
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ى', 'ي')
            .replace('ة', 'ه')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace("&", " and ")
            .replace("t-shirts", "t shirts")
            .replace("t-shirt", "t shirt")
            .replace("tshirt", "t shirt")
        ARABIC_QUERY_SYNONYMS.forEach { (source, target) ->
            value = value.replace(source, " $target ")
        }
        return value
            .replace(Regex("[^a-z0-9.]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun String.searchTokens(): List<String> = split(' ')
        .map { it.trim() }
        .filter { it.length > 1 && it !in STOP_WORDS && it.toDoubleOrNull() == null }
        .distinct()

    private fun String.termVariants(): Set<String> = buildSet {
        add(this@termVariants)
        when {
            this@termVariants == "dresses" -> add("dress")
            this@termVariants == "shoes" -> add("shoe")
            this@termVariants == "sneakers" -> add("sneaker")
            this@termVariants == "shirts" -> add("shirt")
            this@termVariants == "pants" -> add("pant")
            this@termVariants.endsWith("es") && length > 4 -> add(dropLast(2))
            this@termVariants.endsWith("s") && length > 4 -> add(dropLast(1))
        }
        if (this@termVariants == "man") add("men")
        if (this@termVariants == "men") add("man")
        if (this@termVariants == "woman") add("women")
        if (this@termVariants == "women") add("woman")
        if (this@termVariants == "tee") add("t shirt")
        if (this@termVariants == "tshirt") add("t shirt")
        if (this@termVariants == "grey") add("gray")
        if (this@termVariants == "gray") add("grey")
        if (this@termVariants == "trainer") add("sneaker")
        if (this@termVariants == "sneaker") add("trainer")
    }

    private fun String.containsAny(values: Iterable<String>): Boolean = values.any { containsPhrase(it) }

    private fun String.containsAnyPhrase(values: Iterable<String>): Boolean = values.any { containsPhrase(it) }

    private fun String.containsPhrase(value: String): Boolean {
        val phrase = value.normalizedForSearch()
        if (phrase.isBlank()) return false
        return Regex("(^|\\s)${Regex.escape(phrase)}($|\\s)").containsMatchIn(this)
    }

    private data class ProductSearchCriteria(
        val rawQuery: String,
        val normalizedQuery: String,
        val normalizedTerms: List<String>,
        val gender: ProductGender?,
        val requestedKinds: Set<ProductKind>,
        val colors: Set<String>,
        val materials: Set<String>,
        val styleTerms: Set<String>,
        val minPrice: Double?,
        val maxPrice: Double?,
        val isBroadSearch: Boolean,
        val hasProductSearchSignal: Boolean,
        val isCatalogOverview: Boolean,
    )

    private data class CatalogRetrieval(
        val intent: ChatShoppingIntent,
        val context: String,
        val cards: List<ChatProductCard>,
        val fallbackResponse: String,
    )

    private data class CatalogSearchResult(
        val exactMatches: List<ScoredProduct>,
        val closestAlternatives: List<ScoredProduct>,
        val noExactReason: String?,
        val hardFilteredCount: Int,
        val rankedCount: Int,
    )

    private data class CatalogOverview(
        val context: String,
        val cards: List<ChatProductCard>,
        val fallbackResponse: String,
    )

    private data class ComparisonTitleResolution(
        val requestedNames: List<String>,
        val foundProducts: List<Product>,
        val missingNames: List<String>,
    )

    private data class ScoredProduct(
        val product: Product,
        val score: Int,
        val reason: String,
    )

    private data class OutfitSelection(
        val selected: List<OutfitItem>,
        val notes: List<String>,
    )

    private data class OutfitItem(
        val slot: CatalogSlot,
        val product: Product,
        val score: Int,
        val reason: String,
    )

    private data class OutfitOptionScore(
        val requiredFound: Int,
        val totalScore: Int,
    )

    private enum class MatchBucket {
        EXACT,
        ALTERNATIVE,
    }

    private enum class ChatShoppingIntent {
        PRODUCT_SEARCH,
        PRODUCT_COMPARISON,
        OUTFIT_RECOMMENDATION,
        CATALOG_OVERVIEW,
        GENERAL_HELP,
    }

    private enum class ProductGender(val displayName: String) {
        MEN("men"),
        WOMEN("women"),
        KIDS("kids"),
        UNISEX("unisex"),
        UNKNOWN("unknown"),
    }

    private enum class CatalogSlot(val displayName: String) {
        TOP("Top"),
        BOTTOM("Bottom"),
        DRESS("Dress"),
        SHOES("Shoes"),
        BAG_ACCESSORY("Bag/Accessory"),
    }

    private enum class ProductKind(val displayName: String, val keywords: Set<String>) {
        T_SHIRT("t-shirt/shirt/top", setOf("t shirt", "tee", "shirt", "top")),
        SHIRT("shirt/top", setOf("shirt", "top")),
        TOP("top", setOf("top", "blouse", "shirt", "tee", "t shirt")),
        BLOUSE("blouse/top", setOf("blouse", "top")),
        HOODIE("hoodie", setOf("hoodie", "sweatshirt")),
        JACKET("jacket", setOf("jacket", "coat")),
        DRESS("dress", setOf("dress")),
        PANTS("pants/bottom", setOf("pants", "pant", "trousers", "trouser", "bottom", "jeans")),
        JEANS("jeans", setOf("jeans", "denim")),
        TROUSERS("trousers", setOf("trousers", "trouser", "pants")),
        SHORTS("shorts", setOf("shorts", "short")),
        SKIRT("skirt", setOf("skirt")),
        SHOES("shoes/footwear", setOf("shoe", "shoes", "footwear", "sneaker", "boot", "loafer", "sandal", "heel")),
        SNEAKERS("sneakers", setOf("sneaker", "sneakers", "trainer", "trainers")),
        BOOTS("boots", setOf("boot", "boots")),
        LOAFERS("loafers", setOf("loafer", "loafers")),
        SANDALS("sandals", setOf("sandal", "sandals")),
        HEELS("heels", setOf("heel", "heels")),
        BAG("bag", setOf("bag", "bags", "backpack", "purse", "wallet")),
        ACCESSORY("accessory", setOf("accessory", "accessories", "belt", "cap", "hat", "sunglasses")),
    }

    private companion object {
        const val TAG = "WearZoneSmartChat"
        const val MAX_CHAT_PRODUCTS = 8
        const val MAX_COMPARE_PRODUCTS = 4
        const val MAX_FALLBACK_PRODUCTS = 5
        const val FUZZY_TITLE_MIN_SCORE = 65

        val STOP_WORDS = setOf(
            "show", "me", "find", "search", "for", "the", "a", "an", "and", "or", "to", "with", "of", "in", "on",
            "products", "product", "please", "do", "you", "have", "available", "wearzone", "recommend", "want", "need",
            "something", "like", "under", "below", "less", "than", "over", "above", "more", "maximum", "minimum", "max", "min", "egp",
        )
        val PRICE_STOP_WORDS = setOf("under", "below", "less", "than", "over", "above", "more", "maximum", "minimum", "max", "min", "up", "between", "egp")
        val PRODUCT_SEARCH_TERMS = setOf("show", "find", "search", "product", "products", "catalog", "available", "sell", "have", "wearzone")
        val BROAD_SEARCH_TERMS = setOf("all", "catalog", "clothes", "clothing", "products", "product", "what do you sell")
        val CATALOG_OVERVIEW_TERMS = setOf(
            "what products do you have", "what categories do you have", "what do you have", "what do you sell",
            "catalog overview", "show catalog", "show me your catalog", "just these", "is that all", "only these",
            "what available", "have what", "available products", "available categories"
        )
        val COMPARISON_TERMS = setOf("compare", "comparison", "versus", "vs", "which is better", "difference between")
        val COMPARISON_NOISE_TERMS = setOf("compare", "comparison", "versus", "vs", "which is better", "difference between", "better", "why", "and", "or")
        val COMPARISON_EXTRA_STOP_WORDS = setOf("compare", "comparison", "versus", "vs", "which", "better", "why", "one", "the", "this", "that")
        val OUTFIT_TERMS = setOf("outfit", "look", "style me", "recommend me an outfit", "complete set", "match with", "wear with", "casual outfit")

        val MEN_QUERY_KEYWORDS = setOf("man", "men", "mens", "male")
        val WOMEN_QUERY_KEYWORDS = setOf("woman", "women", "womens", "female", "ladies", "lady", "girls", "girl")
        val KIDS_QUERY_KEYWORDS = setOf("kids", "children", "child", "boys", "boy", "girls", "girl")
        val MEN_KEYWORDS = MEN_QUERY_KEYWORDS + setOf("men collection", "menswear")
        val WOMEN_KEYWORDS = WOMEN_QUERY_KEYWORDS + setOf("women collection", "womenswear")
        val KIDS_KEYWORDS = KIDS_QUERY_KEYWORDS + setOf("kids collection")
        val UNISEX_KEYWORDS = setOf("unisex", "all gender", "all genders")

        val COLOR_KEYWORDS = setOf(
            "black", "white", "blue", "navy", "red", "green", "gray", "grey", "brown", "beige", "cream",
            "pink", "yellow", "orange", "purple", "gold", "silver", "maroon", "khaki",
        )
        val MATERIAL_KEYWORDS = setOf("leather", "cotton", "denim", "linen", "wool", "suede", "polyester", "viscose", "knit", "canvas")
        val STYLE_KEYWORDS = setOf("casual", "formal", "semi formal", "summer", "winter", "night", "day", "party", "parties", "sport", "classic", "streetwear")

        val BOOT_KEYWORDS = setOf("boot", "boots")
        val SNEAKER_KEYWORDS = setOf("sneaker", "sneakers", "trainer", "trainers")
        val LOAFER_KEYWORDS = setOf("loafer", "loafers")
        val SANDAL_KEYWORDS = setOf("sandal", "sandals")
        val HEEL_KEYWORDS = setOf("heel", "heels")
        val SHOES_KEYWORDS = setOf("shoe", "shoes", "footwear") + BOOT_KEYWORDS + SNEAKER_KEYWORDS + LOAFER_KEYWORDS + SANDAL_KEYWORDS + HEEL_KEYWORDS
        val SHOES_QUERY_KEYWORDS = SHOES_KEYWORDS + setOf("foot wear")

        val DRESS_KEYWORDS = setOf("dress", "dresses", "gown")
        val T_SHIRT_KEYWORDS = setOf("t shirt", "tee", "tees")
        val SHIRT_KEYWORDS = setOf("shirt", "shirts", "polo")
        val SHIRT_QUERY_KEYWORDS = SHIRT_KEYWORDS
        val BLOUSE_KEYWORDS = setOf("blouse", "blouses")
        val HOODIE_KEYWORDS = setOf("hoodie", "hoodies", "sweatshirt", "sweatshirts")
        val JACKET_KEYWORDS = setOf("jacket", "jackets", "coat", "coats")
        val TOP_KEYWORDS = T_SHIRT_KEYWORDS + SHIRT_KEYWORDS + BLOUSE_KEYWORDS + HOODIE_KEYWORDS + JACKET_KEYWORDS + setOf("top", "tops", "sweater", "sweaters")
        val TOP_QUERY_KEYWORDS = TOP_KEYWORDS

        val PANTS_KEYWORDS = setOf("pant", "pants", "trouser", "trousers")
        val PANTS_QUERY_KEYWORDS = PANTS_KEYWORDS + setOf("bottom", "bottoms")
        val JEANS_KEYWORDS = setOf("jean", "jeans", "denim")
        val SHORTS_KEYWORDS = setOf("short", "shorts")
        val SKIRT_KEYWORDS = setOf("skirt", "skirts")
        val BAG_KEYWORDS = setOf("bag", "bags", "backpack", "purse", "wallet", "handbag")
        val ACCESSORY_KEYWORDS = setOf("accessory", "accessories", "belt", "cap", "hat", "sunglasses", "scarf")
        val ACCESSORY_QUERY_KEYWORDS = ACCESSORY_KEYWORDS

        val PRICE_MAX_PATTERNS = listOf(
            "(?:under|below|less than|max|maximum|up to|no more than|not above|<=|lte)\\s+(\\d+(?:\\.\\d+)?)",
            "(\\d+(?:\\.\\d+)?)\\s*(?:egp)?\\s*(?:or less|and below|max)",
        )
        val PRICE_MIN_PATTERNS = listOf(
            "(?:over|above|more than|min|minimum|from|>=|gte)\\s+(\\d+(?:\\.\\d+)?)",
            "(\\d+(?:\\.\\d+)?)\\s*(?:egp)?\\s*(?:or more|and above|min)",
        )

        val ORDINAL_TERMS = mapOf(
            "first" to 1,
            "second" to 2,
            "third" to 3,
            "fourth" to 4,
            "1st" to 1,
            "2nd" to 2,
            "3rd" to 3,
            "4th" to 4,
        )

        val ARABIC_QUERY_SYNONYMS = linkedMapOf(
            "دورلي" to "search",
            "دور" to "search",
            "ابحث" to "search",
            "بحث" to "search",
            "وريني" to "show",
            "اعرض" to "show",
            "عرض" to "show",
            "هاتلي" to "show",
            "عاوز" to "want",
            "اريد" to "want",
            "منتجات" to "products",
            "منتج" to "product",
            "عندكم" to "have",
            "عندك" to "have",
            "ايه" to "what",
            "اي" to "what",
            "الموجوده" to "available",
            "الموجود" to "available",
            "موجوده" to "available",
            "موجود" to "available",
            "بس" to "only",
            "دول" to "these",
            "دي" to "these",
            "هل ده كل" to "is that all",
            "رجالي" to "men",
            "رجال" to "men",
            "للرجال" to "men",
            "رجل" to "man",
            "اولادي" to "kids boy",
            "حريمي" to "women",
            "نسائي" to "women",
            "نسويه" to "women",
            "نساء" to "women",
            "للسيدات" to "women",
            "سيدات" to "women",
            "بنات" to "women girls",
            "للبنات" to "women girls",
            "بناتي" to "kids girl",
            "اطفال" to "kids",
            "اطفالي" to "kids",
            "ولادي" to "kids",
            "الاحذيه" to "shoes footwear",
            "احذيه" to "shoes footwear",
            "حذاء" to "shoes footwear",
            "جزمه" to "shoes footwear",
            "جزم" to "shoes footwear",
            "شوز" to "shoes footwear",
            "بوت" to "boots",
            "جزمه بوت" to "boots",
            "تيشيرت" to "t shirt shirt top",
            "تي شيرت" to "t shirt shirt top",
            "تشيرت" to "t shirt shirt top",
            "قميص" to "shirt top",
            "قمصان" to "shirts tops",
            "بلوزه" to "blouse top",
            "فستان" to "dress",
            "فساتين" to "dresses",
            "بنطلون" to "pants jeans trousers",
            "بناطيل" to "pants jeans trousers",
            "جينز" to "jeans denim",
            "شورت" to "shorts",
            "جيبه" to "skirt",
            "شنطه" to "bag",
            "شنط" to "bags",
            "حقيبه" to "bag",
            "اكسسوارات" to "accessories",
            "اكسسوار" to "accessory",
            "اسود" to "black",
            "سوداء" to "black",
            "ابيض" to "white",
            "بيضاء" to "white",
            "ازرق" to "blue",
            "كحلي" to "navy",
            "احمر" to "red",
            "اخضر" to "green",
            "رمادي" to "gray",
            "رصاصي" to "gray",
            "بني" to "brown",
            "بيج" to "beige",
            "كريمي" to "cream",
            "وردي" to "pink",
            "اصفر" to "yellow",
            "برتقالي" to "orange",
            "بنفسجي" to "purple",
            "ذهبي" to "gold",
            "فضي" to "silver",
            "جلد" to "leather",
            "قطن" to "cotton",
            "كتان" to "linen",
            "صوف" to "wool",
            "دنيم" to "denim",
            "كاجوال" to "casual",
            "رسمي" to "formal",
            "فورمال" to "formal",
            "صيفي" to "summer",
            "شتوي" to "winter",
            "اقل من" to "under",
            "اقل" to "under",
            "تحت" to "under",
            "لحد" to "up to",
            "حتي" to "up to",
            "فوق" to "over",
            "اكثر من" to "over",
        )
    }
}
