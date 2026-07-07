package com.example.wearzone.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wearzone.data.local.search.RecentSearchDao
import com.example.wearzone.data.local.dao.CartDao
import com.example.wearzone.data.local.entity.CartItemEntity
import com.example.wearzone.data.local.search.RecentSearchEntity
import com.example.wearzone.data.local.wishlist.WishlistDao
import com.example.wearzone.data.local.wishlist.WishlistEntity
@Database(entities = [WearZoneEntity::class, RecentSearchEntity::class, WishlistEntity::class, CartItemEntity::class], version = 6)
abstract class WearZoneDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
}