package com.example.wearzone.domain.product.model

data class ProductDetail(
    val id: Long,
    val title: String,
    val vendor: String,
    val descriptionHtml: String,
    val price: String,
    val images: List<String>,
    val availableSizes: List<String>,
    // JETS Lab Requirement 2
    val rating: Double,
    val reviewsCount: Int,
    // JETS Lab Requirement 4
    val isFavorite: Boolean
)
