package com.example.wearzone.data.local.wishlist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wearzone.domain.wishlist.model.WishlistItem

@Entity(tableName = "wishlist_table")
data class WishlistEntity(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val vendor: String = "",
    val price: String = "",
    val currencyCode: String = "",
    val imageUrl: String = "",
    val isOutOfStock: Boolean = false,
    val userId: String = "" // Partition by user
)

fun WishlistEntity.toDomainModel() = WishlistItem(
    id = id,
    title = title,
    vendor = vendor,
    price = price,
    currencyCode = currencyCode,
    imageUrl = imageUrl,
    isOutOfStock = isOutOfStock
)

fun WishlistItem.toEntity(userId: String) = WishlistEntity(
    id = id,
    title = title,
    vendor = vendor,
    price = price,
    currencyCode = currencyCode,
    imageUrl = imageUrl,
    isOutOfStock = isOutOfStock,
    userId = userId
)
