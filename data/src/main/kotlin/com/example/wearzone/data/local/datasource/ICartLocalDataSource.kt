package com.example.wearzone.data.local.datasource

import com.example.wearzone.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

interface ICartLocalDataSource {
    fun observeCartItems(): Flow<List<CartItemEntity>>
    suspend fun addOrUpdateItem(item: CartItemEntity)
    suspend fun updateQuantity(variantId: String, quantity: Int)
    suspend fun removeItem(variantId: String)
    suspend fun clearCart()
}
