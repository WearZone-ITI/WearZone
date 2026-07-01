package com.example.wearzone.domain.wishlist.repository

import com.example.wearzone.domain.wishlist.model.WishlistItem
import kotlinx.coroutines.flow.Flow

interface IWishlistRepository {
    /**
     * Returns a Flow of the user's wishlist from the local SSOT (Room DB).
     */
    fun getWishlistFlow(userId: String): Flow<List<WishlistItem>>

    /**
     * Toggles the favorite status of a product.
     * Optimistically updates the local DB, then syncs with Firebase.
     */
    suspend fun toggleFavorite(item: WishlistItem, userId: String)

    /**
     * Syncs the local wishlist with the remote Firebase Realtime Database.
     */
    suspend fun syncWithRemote(userId: String)
}
