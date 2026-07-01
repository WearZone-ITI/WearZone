package com.example.wearzone.data.local.wishlist

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_table WHERE userId = :userId")
    fun getWishlistFlow(userId: String): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist_table WHERE userId = :userId")
    suspend fun getWishlist(userId: String): List<WishlistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WishlistEntity)

    @Query("DELETE FROM wishlist_table WHERE id = :productId AND userId = :userId")
    suspend fun deleteItem(productId: String, userId: String)

    @Query("SELECT * FROM wishlist_table WHERE id = :productId AND userId = :userId LIMIT 1")
    suspend fun getItem(productId: String, userId: String): WishlistEntity?
}
