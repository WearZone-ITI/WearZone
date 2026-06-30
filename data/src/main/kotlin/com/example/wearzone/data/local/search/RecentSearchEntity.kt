package com.example.wearzone.data.local.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchEntity {
    @Query("SELECT * FROM recent_searches ORDER BY searched_at DESC LIMIT :limit")
    fun observeRecentSearches(limit: Int = 8): Flow<List<RecentSearchDto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentSearch(entity: RecentSearchDto)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()

    @Query("DELETE FROM recent_searches WHERE query NOT IN (SELECT query FROM recent_searches ORDER BY searched_at DESC LIMIT :limit)")
    suspend fun pruneOldSearches(limit: Int = 8)
}
