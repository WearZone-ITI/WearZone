package com.example.wearzone.data.local.home

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HomeCacheDao {
    @Query("SELECT jsonContent FROM home_cache WHERE `key` = :key")
    suspend fun getCache(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCache(cache: HomeCacheEntity)
}
