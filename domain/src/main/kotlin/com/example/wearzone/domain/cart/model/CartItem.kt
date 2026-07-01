package com.example.wearzone.domain.cart.model

data class CartItem(
    val variantId: String,
    val productId: String,
    val title: String,
    val vendor: String,
    val price: Double,
    val currencyCode: String,
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String?,
    val size: String?,
)
