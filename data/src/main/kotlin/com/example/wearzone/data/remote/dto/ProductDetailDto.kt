package com.example.wearzone.data.remote.dto

import com.example.wearzone.domain.product.model.ProductDetail
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailDto(
    val product: ShopifyProductDetail
)

@Serializable
data class ShopifyProductDetail(
    val id: Long,
    val title: String,
    val vendor: String,
    val body_html: String,
    val images: List<ImageDto>,
    val variants: List<VariantDto>
) {
    fun toDomain(rating: Double, reviewsCount: Int, isFavorite: Boolean): ProductDetail {
        val firstVariant = variants.firstOrNull()
        val priceDouble = firstVariant?.price?.toDoubleOrNull() ?: 0.0
        
        return ProductDetail(
            id = id.toString(),
            variantId = firstVariant?.id?.toString() ?: id.toString(),
            title = title,
            vendor = vendor,
            descriptionHtml = body_html,
            price = priceDouble,
            currencyCode = "EGP", // Will add currency prefix in UI if needed, but string format comes from Shopify
            images = images.mapNotNull { it.src },
            availableSizes = variants.mapNotNull { it.option1 }.distinct(),
            rating = rating,
            reviewsCount = reviewsCount,
            isFavorite = isFavorite
        )
    }
}

@Serializable
data class ImageDto(
    val src: String? = null
)

@Serializable
data class VariantDto(
    val id: Long? = null,
    val price: String? = null,
    val option1: String? = null
)
