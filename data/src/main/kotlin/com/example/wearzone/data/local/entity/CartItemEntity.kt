package com.example.wearzone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wearzone.domain.cart.model.CartItem

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
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

fun CartItemEntity.toDomain(): CartItem = CartItem(
    variantId = variantId,
    productId = productId,
    title = title,
    vendor = vendor,
    price = price,
    currencyCode = currencyCode,
    quantity = quantity,
    maxQuantity = maxQuantity,
    imageUrl = imageUrl,
    size = size,
)

fun CartItem.toEntity(): CartItemEntity = CartItemEntity(
    variantId = variantId,
    productId = productId,
    title = title,
    vendor = vendor,
    price = price,
    currencyCode = currencyCode,
    quantity = quantity.coerceIn(1, maxQuantity.coerceAtLeast(1)),
    maxQuantity = maxQuantity.coerceAtLeast(1),
    imageUrl = imageUrl,
    size = size,
)
