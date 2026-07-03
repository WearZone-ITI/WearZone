package com.example.wearzone.domain.common

data class Category(
    val id: Long,
    val title: String,
    val imageUrl: String?,
    val productsCount: Int?
)