package com.example.wearzone.domain.product.model

data class ProductDetail(
    val id: String,
    val variantId: String,
    val title: String,
    val vendor: String,
    val descriptionHtml: String,
    val price: Double,
    val currencyCode: String,
    val images: List<String>,
    val availableSizes: List<String>,
    val rating: Double,
    val reviewsCount: Int,
    val isFavorite: Boolean,
    val variants: List<ProductVariant> = emptyList(),
    val availableColors: List<String> = emptyList(),
    val isOutOfStock: Boolean = false,
)

data class ProductVariant(
    val id: String,
    val title: String,
    val price: Double,
    val availableQuantity: Int,
    val size: String?,
    val color: String?,
    val imageUrl: String?,
) {
    val isAvailable: Boolean get() = availableQuantity > 0
}
