package com.example.wearzone.domain.product.model

data class Product(
    val id: String,
    val title: String,
    val vendor: String,
    val price: Double,
    val currencyCode: String,
    val imageUrl: String?,
    val isFavorite: Boolean = false
)
