package com.example.wearzone.domain.cart.repository

import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.common.DataResult
import kotlinx.coroutines.flow.Flow

interface ICartRepository {
    fun observeCart(): Flow<List<CartItem>>
    suspend fun addToCart(item: CartItem): DataResult<Unit>
    suspend fun removeFromCart(variantId: String): DataResult<Unit>
    suspend fun updateQuantity(variantId: String, quantity: Int): DataResult<Unit>
    suspend fun clearCart(): DataResult<Unit>
}
