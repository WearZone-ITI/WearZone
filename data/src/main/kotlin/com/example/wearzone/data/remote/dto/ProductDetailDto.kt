package com.example.wearzone.data.remote.dto

import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.model.ProductVariant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailDto(
    val product: ShopifyProductDetail,
)

@Serializable
data class ShopifyProductDetail(
    val id: Long,
    val title: String,
    val vendor: String = "",
    @SerialName("body_html") val bodyHtml: String? = null,
    @SerialName("product_type") val productType: String? = null,
    val tags: String? = null,
    val images: List<ImageDto> = emptyList(),
    val image: ImageDto? = null,
    val options: List<ProductOptionDto> = emptyList(),
    val variants: List<VariantDto> = emptyList(),
) {
    fun toDomain(rating: Double, reviewsCount: Int, isFavorite: Boolean): ProductDetail {
        val sizeOptionIndex = optionIndex("Size") ?: 0
        val colorOptionIndex = optionIndex("Color")
        val domainVariants = variants.map { variant ->
            variant.toDomain(
                images = images,
                fallbackImageUrl = image?.src,
                sizeOptionIndex = sizeOptionIndex,
                colorOptionIndex = colorOptionIndex,
            )
        }
        val firstAvailableVariant = domainVariants.firstOrNull { it.isAvailable } ?: domainVariants.firstOrNull()
        val priceDouble = firstAvailableVariant?.price ?: variants.firstOrNull()?.price?.toDoubleOrNull() ?: 0.0
        val galleryImages = buildList {
            image?.src?.takeIf { it.isNotBlank() }?.let(::add)
            images.mapNotNullTo(this) { it.src?.takeIf(String::isNotBlank) }
            domainVariants.mapNotNullTo(this) { it.imageUrl?.takeIf(String::isNotBlank) }
        }.distinct()
        val sizes = variants
            .mapNotNull { it.optionAt(sizeOptionIndex).cleanOptionValue() }
            .distinctBy { it.lowercase() }
        val colors = colorOptionIndex?.let { index ->
            variants.mapNotNull { it.optionAt(index).cleanOptionValue() }.distinctBy { it.lowercase() }
        }.orEmpty()

        return ProductDetail(
            id = id.toString(),
            variantId = firstAvailableVariant?.id ?: variants.firstOrNull()?.id?.toString() ?: id.toString(),
            title = title,
            vendor = vendor,
            descriptionHtml = bodyHtml.toPlainText(),
            price = priceDouble,
            currencyCode = "EGP",
            images = galleryImages,
            availableSizes = sizes,
            rating = rating,
            reviewsCount = reviewsCount,
            isFavorite = isFavorite,
            variants = domainVariants,
            availableColors = colors,
            isOutOfStock = domainVariants.isNotEmpty() && domainVariants.none { it.isAvailable },
        )
    }

    private fun optionIndex(optionName: String): Int? = options
        .indexOfFirst { it.name.equals(optionName, ignoreCase = true) }
        .takeIf { it >= 0 }
}

@Serializable
data class ProductOptionDto(
    val id: Long? = null,
    val name: String = "",
    val position: Int? = null,
    val values: List<String> = emptyList(),
)

@Serializable
data class ImageDto(
    val id: Long? = null,
    val src: String? = null,
    @SerialName("variant_ids") val variantIds: List<Long> = emptyList(),
)

@Serializable
data class VariantDto(
    val id: Long? = null,
    val title: String? = null,
    val price: String? = null,
    val option1: String? = null,
    val option2: String? = null,
    val option3: String? = null,
    @SerialName("inventory_quantity") val inventoryQuantity: Int? = null,
    @SerialName("image_id") val imageId: Long? = null,
) {
    fun toDomain(
        images: List<ImageDto>,
        fallbackImageUrl: String?,
        sizeOptionIndex: Int,
        colorOptionIndex: Int?,
    ): ProductVariant {
        val imageUrl = imageId?.let { imageId -> images.firstOrNull { it.id == imageId }?.src }
            ?: id?.let { variantId -> images.firstOrNull { variantId in it.variantIds }?.src }
            ?: fallbackImageUrl
            ?: images.firstOrNull()?.src

        return ProductVariant(
            id = id?.toString().orEmpty(),
            title = title.orEmpty(),
            price = price?.toDoubleOrNull() ?: 0.0,
            availableQuantity = (inventoryQuantity ?: 0).coerceAtLeast(0),
            size = optionAt(sizeOptionIndex).cleanOptionValue(),
            color = colorOptionIndex?.let { optionAt(it).cleanOptionValue() },
            imageUrl = imageUrl,
        )
    }

    fun optionAt(index: Int): String? = when (index) {
        0 -> option1
        1 -> option2
        2 -> option3
        else -> null
    }
}

private fun String?.cleanOptionValue(): String? = this
    ?.trim()
    ?.takeIf { it.isNotBlank() }
    ?.takeUnless { it.equals("Default Title", ignoreCase = true) || it.equals("Default", ignoreCase = true) }

private fun String?.toPlainText(): String = this
    .orEmpty()
    .replace(Regex("<\\s*br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("<\\s*/p\\s*>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("<\\s*/li\\s*>", RegexOption.IGNORE_CASE), "\n")
    .replace(Regex("<\\s*li[^>]*>", RegexOption.IGNORE_CASE), "- ")
    .replace(Regex("<[^>]*>"), "")
    .replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#039;", "'")
    .replace("&#39;", "'")
    .replace(Regex("[ \\t]+"), " ")
    .replace(Regex("\\n\\s+"), "\n")
    .replace(Regex("\\n{3,}"), "\n\n")
    .trim()
