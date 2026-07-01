package com.example.wearzone.domain.wishlist.model

data class WishlistItem(
    val id: String,
    val title: String,
    val vendor: String,
    val price: String,
    val currencyCode: String,
    val imageUrl: String,
    val isOutOfStock: Boolean
)
