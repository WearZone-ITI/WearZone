package com.example.wearzone.data.local.datasource

import com.example.wearzone.data.local.dao.CartDao
import com.example.wearzone.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CartLocalDataSourceImpl @Inject constructor(
    private val cartDao: CartDao,
) : ICartLocalDataSource {

    override fun observeCartItems(): Flow<List<CartItemEntity>> = cartDao.observeCartItems()

    override suspend fun addOrUpdateItem(item: CartItemEntity) {
        val existingItem = cartDao.getCartItem(item.variantId)
        val quantity = if (existingItem == null) {
            item.quantity
        } else {
            existingItem.quantity + item.quantity
        }.coerceIn(1, item.maxQuantity.coerceAtLeast(1))

        cartDao.upsertCartItem(item.copy(quantity = quantity))
    }

    override suspend fun updateQuantity(variantId: String, quantity: Int) {
        val existingItem = cartDao.getCartItem(variantId) ?: return
        cartDao.updateQuantity(
            variantId = variantId,
            quantity = quantity.coerceIn(1, existingItem.maxQuantity.coerceAtLeast(1)),
        )
    }

    override suspend fun removeItem(variantId: String) {
        cartDao.deleteCartItem(variantId)
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }
}
