package com.example.wearzone.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wearzone.data.local.search.RecentSearchEntity
import com.example.wearzone.data.local.search.RecentSearchDto

@Database(entities = [WearZoneEntity::class, RecentSearchDto::class], version = 2)
abstract class WearZoneDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchEntity
}