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
    val isFavorite: Boolean
)
