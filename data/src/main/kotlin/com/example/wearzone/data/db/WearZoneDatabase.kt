package com.example.wearzone.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wearzone.data.local.search.RecentSearchDao
import com.example.wearzone.data.local.search.RecentSearchEntity
import com.example.wearzone.data.local.dao.CartDao
import com.example.wearzone.data.local.entity.CartItemEntity

@Database(entities = [WearZoneEntity::class, RecentSearchEntity::class,CartItemEntity::class], version = 3)
abstract class WearZoneDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun cartDao(): CartDao
}