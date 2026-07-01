package com.example.wearzone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wearzone.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY title ASC")
    fun observeCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE variantId = :variantId LIMIT 1")
    suspend fun getCartItem(variantId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE variantId = :variantId")
    suspend fun updateQuantity(variantId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE variantId = :variantId")
    suspend fun deleteCartItem(variantId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
