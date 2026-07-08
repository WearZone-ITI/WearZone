package com.example.wearzone.data.remote.dto

import com.example.wearzone.data.localization.CatalogLocalization
import com.example.wearzone.data.localization.cleanText
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val DASHBOARD_CATEGORY_MARKER = "<!-- dashboard:category -->"
private const val DASHBOARD_BRAND_MARKER = "<!-- dashboard:brand -->"

private val MAIN_SECTION_ORDER = listOf("Men", "Women", "Kids", "Shoes", "Bags & Accessories")
private val KNOWN_CATEGORY_TITLES = setOf(
    "Men",
    "Women",
    "Kids",
    "Shoes",
    "Bags & Accessories",
    "T-Shirts",
    "Shirts",
    "Casual Shirts",
    "Formal Shirts",
    "Hoodies",
    "Jackets",
    "Pants",
    "Jeans",
    "Shorts",
    "Dresses",
    "Skirts",
    "Tops",
    "Bags",
    "Accessories",
    "Sneakers",
    "New Arrivals",
)

@Serializable
data class CategoryDto(
    val id: Long,
    val title: String,
    val imageUrl: String? = null,
    val productsCount: Int? = null,
    val titleEn: String = title,
    val titleAr: String = CatalogLocalization.arabicCategoryFor(title),
) {
    fun toDomain(languageCode: String? = null): Category {
        val englishTitle = titleEn.ifBlank { title }
        val arabicTitle = titleAr.ifBlank { CatalogLocalization.arabicCategoryFor(englishTitle) }
        return Category(
            id = id,
            title = CatalogLocalization.selectLocalized(
                english = englishTitle,
                arabic = arabicTitle,
                languageCode = languageCode,
                fallback = title,
            ),
            imageUrl = imageUrl,
            productsCount = productsCount,
            titleEn = englishTitle,
            titleAr = arabicTitle,
        )
    }
}

@Serializable
data class BrandDto(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
) {
    fun toDomain(): Brand = Brand(id, title, imageUrl)
}

@Serializable
data class ProductDto(
    val id: String,
    val variantId: String,
    val title: String,
    val vendor: String,
    val price: Double,
    val currencyCode: String,
    val imageUrl: String? = null,
    val productType: String = "",
    val tags: List<String> = emptyList(),
    val maxQuantity: Int = 0,
    val isOutOfStock: Boolean = false,
    val size: String? = null,
    val color: String? = null,
    val titleEn: String = title,
    val titleAr: String = "",
    val descriptionEn: String = "",
    val descriptionAr: String = "",
    val categoryEn: String = "",
    val categoryAr: String = "",
    val productTypeEn: String = productType,
    val productTypeAr: String = "",
) {
    fun toDomain(languageCode: String? = null): Product {
        val englishTitle = titleEn.ifBlank { title }
        val englishProductType = productTypeEn.ifBlank { productType }
        val englishCategory = categoryEn.ifBlank { englishProductType }
        val arabicCategory = categoryAr.ifBlank { CatalogLocalization.arabicCategoryFor(englishCategory) }
        val arabicProductType = productTypeAr.ifBlank { CatalogLocalization.arabicCategoryFor(englishProductType) }

        return Product(
            id = id,
            variantId = variantId,
            title = CatalogLocalization.selectLocalized(
                english = englishTitle,
                arabic = titleAr,
                languageCode = languageCode,
                fallback = title,
            ),
            vendor = vendor,
            price = price,
            currencyCode = currencyCode,
            imageUrl = imageUrl,
            isFavorite = false,
            productType = CatalogLocalization.selectLocalized(
                english = englishProductType,
                arabic = arabicProductType,
                languageCode = languageCode,
                fallback = productType,
            ),
            tags = tags,
            maxQuantity = maxQuantity,
            isOutOfStock = isOutOfStock,
            size = size,
            color = color,
            titleEn = englishTitle,
            titleAr = titleAr.cleanText(),
            descriptionEn = descriptionEn.cleanText(),
            descriptionAr = descriptionAr.cleanText(),
            categoryEn = englishCategory,
            categoryAr = arabicCategory,
            productTypeEn = englishProductType,
            productTypeAr = arabicProductType,
        )
    }
}

@Serializable
data class CustomCollectionsResponse(val custom_collections: List<ShopifyCollection> = emptyList())

@Serializable
data class SmartCollectionsResponse(val smart_collections: List<ShopifyCollection> = emptyList())

@Serializable
data class ShopifyCollection(
    val id: Long,
    val title: String,
    val handle: String? = null,
    @SerialName("body_html") val bodyHtml: String? = null,
    val image: ShopifyImage? = null,
    @SerialName("products_count") val productsCount: Int? = null,
) {
    val isDashboardCategory: Boolean
        get() = bodyHtml.orEmpty().contains(DASHBOARD_CATEGORY_MARKER) ||
            (bodyHtml.orEmpty().contains(DASHBOARD_BRAND_MARKER).not() && title in KNOWN_CATEGORY_TITLES)

    val isDashboardBrand: Boolean
        get() = bodyHtml.orEmpty().contains(DASHBOARD_BRAND_MARKER)

    fun toCategoryDto() = CategoryDto(
        id = id,
        title = title,
        imageUrl = image?.src,
        productsCount = productsCount,
        titleEn = title,
        titleAr = CatalogLocalization.arabicCategoryFor(title),
    )

    fun toBrandDto() = BrandDto(id.toString(), title, image?.src)
}

@Serializable
data class ShopifyImage(
    val id: Long? = null,
    val src: String? = null,
    @SerialName("variant_ids") val variantIds: List<Long> = emptyList(),
)

@Serializable
data class ProductsResponse(val products: List<ShopifyProduct> = emptyList())

@Serializable
data class ProductMetafieldsResponse(val metafields: List<ShopifyMetafield> = emptyList())

@Serializable
data class ShopifyMetafield(
    val id: Long? = null,
    val namespace: String? = null,
    val key: String,
    val value: String? = null,
    val type: String? = null,
)

@Serializable
data class ProductLocalizationFields(
    val titleEn: String = "",
    val titleAr: String = "",
    val descriptionEn: String = "",
    val descriptionAr: String = "",
    val categoryEn: String = "",
    val categoryAr: String = "",
    val productTypeEn: String = "",
    val productTypeAr: String = "",
) {
    fun withProductFallbacks(
        title: String,
        description: String = "",
        productType: String = "",
    ): ProductLocalizationFields {
        val resolvedCategoryEn = categoryEn.ifBlank { productType }
        val resolvedProductTypeEn = productTypeEn.ifBlank { productType }
        return copy(
            titleEn = titleEn.ifBlank { title }.cleanText(),
            titleAr = titleAr.cleanText(),
            descriptionEn = descriptionEn.ifBlank { description }.cleanText(),
            descriptionAr = descriptionAr.cleanText(),
            categoryEn = resolvedCategoryEn.cleanText(),
            categoryAr = categoryAr.ifBlank { CatalogLocalization.arabicCategoryFor(resolvedCategoryEn) }.cleanText(),
            productTypeEn = resolvedProductTypeEn.cleanText(),
            productTypeAr = productTypeAr.ifBlank { CatalogLocalization.arabicCategoryFor(resolvedProductTypeEn) }.cleanText(),
        )
    }
}

fun List<ShopifyMetafield>.toProductLocalizationFields(): ProductLocalizationFields {
    val values = filter { it.namespace == null || it.namespace == "wearzone" }
        .associate { it.key to it.value.orEmpty() }
    return ProductLocalizationFields(
        titleEn = values["title_en"].orEmpty(),
        titleAr = values["title_ar"].orEmpty(),
        descriptionEn = values["description_en"].orEmpty(),
        descriptionAr = values["description_ar"].orEmpty(),
        categoryEn = values["category_en"].orEmpty(),
        categoryAr = values["category_ar"].orEmpty(),
        productTypeEn = values["product_type_en"].orEmpty(),
        productTypeAr = values["product_type_ar"].orEmpty(),
    )
}

@Serializable
data class ShopifyProduct(
    val id: Long,
    val title: String,
    val vendor: String = "",
    @SerialName("product_type") val productType: String? = null,
    val tags: String? = null,
    val status: String? = null,
    val variants: List<ShopifyVariant> = emptyList(),
    val image: ShopifyImage? = null,
    val images: List<ShopifyImage> = emptyList(),
) {
    fun toProductDto(
        localization: ProductLocalizationFields = ProductLocalizationFields(),
    ): ProductDto {
        val selectedVariant = variants.firstOrNull { it.availableQuantity > 0 } ?: variants.firstOrNull()
        val firstImage = selectedVariant?.imageId?.let { variantImageId ->
            images.firstOrNull { it.id == variantImageId }?.src
        } ?: image?.src ?: images.firstOrNull()?.src
        val availableQuantity = selectedVariant?.availableQuantity ?: 0
        val outOfStock = variants.isNotEmpty() && variants.none { it.availableQuantity > 0 }
        val productTypeValue = productType.orEmpty()
        val resolvedLocalization = localization.withProductFallbacks(
            title = title,
            productType = productTypeValue,
        )

        return ProductDto(
            id = id.toString(),
            variantId = selectedVariant?.id?.toString() ?: id.toString(),
            title = title,
            vendor = vendor,
            price = selectedVariant?.price?.toDoubleOrNull() ?: 0.0,
            currencyCode = "EGP",
            imageUrl = firstImage,
            productType = productTypeValue,
            tags = parseTags(tags),
            maxQuantity = availableQuantity.coerceAtLeast(0),
            isOutOfStock = outOfStock,
            size = selectedVariant?.sizeValue,
            color = selectedVariant?.colorValue,
            titleEn = resolvedLocalization.titleEn,
            titleAr = resolvedLocalization.titleAr,
            descriptionEn = resolvedLocalization.descriptionEn,
            descriptionAr = resolvedLocalization.descriptionAr,
            categoryEn = resolvedLocalization.categoryEn,
            categoryAr = resolvedLocalization.categoryAr,
            productTypeEn = resolvedLocalization.productTypeEn,
            productTypeAr = resolvedLocalization.productTypeAr,
        )
    }
}

@Serializable
data class ShopifyVariant(
    val id: Long? = null,
    val title: String? = null,
    val price: String? = null,
    val option1: String? = null,
    val option2: String? = null,
    val option3: String? = null,
    @SerialName("inventory_quantity") val inventoryQuantity: Int? = null,
    @SerialName("image_id") val imageId: Long? = null,
) {
    val availableQuantity: Int get() = inventoryQuantity ?: 0
    val sizeValue: String? get() = option1.cleanOptionValue()
    val colorValue: String? get() = option2.cleanOptionValue()
}

fun List<ShopifyCollection>.dashboardCategories(): List<CategoryDto> = this
    .filter { it.isDashboardCategory }
    .distinctBy { it.title.trim().lowercase() }
    .sortedWith(compareBy<ShopifyCollection> { collection ->
        val index = MAIN_SECTION_ORDER.indexOf(collection.title)
        if (index >= 0) index else MAIN_SECTION_ORDER.size + 1
    }.thenBy { it.title })
    .map { it.toCategoryDto() }

fun List<ShopifyCollection>.dashboardBrands(): List<BrandDto> = this
    .filter { it.isDashboardBrand }
    .distinctBy { it.title.trim().lowercase() }
    .sortedWith(compareByDescending<ShopifyCollection> { it.productsCount ?: 0 }.thenBy { it.title })
    .map { it.toBrandDto() }

fun List<ShopifyCollection>.smartBrandsFallback(): List<BrandDto> = this
    .filter { (it.productsCount ?: 0) > 0 || it.title.isNotBlank() }
    .distinctBy { it.title.trim().lowercase() }
    .sortedWith(compareByDescending<ShopifyCollection> { it.productsCount ?: 0 }.thenBy { it.title })
    .map { it.toBrandDto() }

fun List<ShopifyProduct>.vendorBrands(): List<BrandDto> = this
    .filter { it.vendor.isNotBlank() }
    .groupBy { it.vendor.trim().lowercase() }
    .map { (_, products) ->
        val first = products.first()
        BrandDto(
            id = first.vendor.trim(),
            title = first.vendor.trim(),
            imageUrl = products.firstNotNullOfOrNull { product ->
                product.image?.src ?: product.images.firstOrNull()?.src
            },
        )
    }
    .sortedWith(compareBy<BrandDto> { it.title.lowercase() })

fun List<BrandDto>.withVendorImages(vendorBrands: List<BrandDto>): List<BrandDto> {
    if (vendorBrands.isEmpty()) return emptyList()
    if (isEmpty()) return vendorBrands

    val currentVendorsByTitle = vendorBrands.associateBy { it.title.trim().lowercase() }
    return mapNotNull { brand ->
        val vendorBrand = currentVendorsByTitle[brand.title.trim().lowercase()] ?: return@mapNotNull null
        BrandDto(
            id = vendorBrand.id,
            title = vendorBrand.title,
            imageUrl = brand.imageUrl?.takeIf { it.isNotBlank() } ?: vendorBrand.imageUrl,
        )
    }
        .distinctBy { it.title.trim().lowercase() }
        .sortedWith(compareBy<BrandDto> { it.title.lowercase() })
}

private fun parseTags(value: String?): List<String> = value
    .orEmpty()
    .split(',')
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinctBy { it.lowercase() }

private fun String?.cleanOptionValue(): String? = this
    ?.trim()
    ?.takeIf { it.isNotBlank() }
    ?.takeUnless { it.equals("Default Title", ignoreCase = true) || it.equals("Default", ignoreCase = true) }
