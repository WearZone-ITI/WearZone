package com.example.wearzone.data.repository

import com.example.wearzone.data.local.wishlist.WishlistDao
import com.example.wearzone.data.local.wishlist.WishlistEntity
import com.example.wearzone.data.local.wishlist.toDomainModel
import com.example.wearzone.data.local.wishlist.toEntity
import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.repository.IWishlistRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WishlistRepositoryImpl @Inject constructor(
    private val wishlistDao: WishlistDao
) : IWishlistRepository {

    private val database = FirebaseDatabase.getInstance().reference

    override fun getWishlistFlow(userId: String): Flow<List<WishlistItem>> = callbackFlow {
        val userWishlistRef = database.child("users").child(userId).child("wishlist")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                launch {
                    try {
                        val remoteItems = snapshot.children.mapNotNull { it.getValue(WishlistEntity::class.java) }
                        val localItems = wishlistDao.getWishlist(userId)
                        val remoteIds = remoteItems.map { it.id }.toSet()
                        
                        // Delete items missing from remote
                        localItems.forEach { 
                            if (it.id !in remoteIds) wishlistDao.deleteItem(it.id, userId) 
                        }
                        // Insert/update remote items
                        remoteItems.forEach { 
                            wishlistDao.insertItem(it.copy(userId = userId)) 
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("WishlistRepo", "Database error: ${error.message}", error.toException())
            }
        }
        
        userWishlistRef.addValueEventListener(listener)
        
        val job = launch {
            wishlistDao.getWishlistFlow(userId).collect { entities ->
                send(entities.map { it.toDomainModel() })
            }
        }
        
        awaitClose {
            userWishlistRef.removeEventListener(listener)
            job.cancel()
        }
    }

    override suspend fun toggleFavorite(item: WishlistItem, userId: String) {
        val existingItem = wishlistDao.getItem(item.id, userId)
        val isCurrentlyFavorite = existingItem != null

        // 1. Optimistic Update (Room DB)
        if (isCurrentlyFavorite) {
            wishlistDao.deleteItem(item.id, userId)
        } else {
            wishlistDao.insertItem(item.toEntity(userId))
        }

        // 2. Sync with Firebase (Fire and forget, Firebase handles offline queuing)
        val userWishlistRef = database.child("users").child(userId).child("wishlist")
        try {
            if (isCurrentlyFavorite) {
                userWishlistRef.child(item.id).removeValue()
            } else {
                userWishlistRef.child(item.id).setValue(item.toEntity(userId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun syncWithRemote(userId: String) {
        try {
            val userWishlistRef = database.child("users").child(userId).child("wishlist")
            val snapshot = userWishlistRef.get().await()
            
            // Note: In a complete implementation we might want to diff the local and remote, 
            // but for simplicity, we pull remote down to ensure consistency.
            if (snapshot.exists()) {
                val remoteItems = snapshot.children.mapNotNull { it.getValue(WishlistEntity::class.java) }
                // For a robust sync, we could clear local for this user and insert remote
                // (or merge them properly). Let's do a simple overwrite for this user.
                remoteItems.forEach {
                    wishlistDao.insertItem(it.copy(userId = userId))
                }
            }
        } catch (e: Exception) {
            // Log sync failure
        }
    }
}
