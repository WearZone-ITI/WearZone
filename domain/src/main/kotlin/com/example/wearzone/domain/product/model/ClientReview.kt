package com.example.wearzone.domain.product.model

data class ClientReview(
    val id: String,
    val productId: String,
    val shopperName: String,
    val rating: Double,
    val comment: String,
    val timestamp: Long
)
