package com.example.wearzone.domain.product.model

data class ProductDetail(
    val id: Long,
    val title: String,
    val vendor: String,
    val descriptionHtml: String,
    val price: String,
    val images: List<String>,
    val availableSizes: List<String>,
    val rating: Double,
    val reviewsCount: Int,
    val isFavorite: Boolean
)
