package com.example.wearzone.domain.search.repository

import com.example.wearzone.domain.search.model.RecentSearch
import kotlinx.coroutines.flow.Flow

interface IRecentSearchRepository {
    fun observeRecentSearches(): Flow<List<RecentSearch>>
    suspend fun saveRecentSearch(query: String): Result<Unit>
    suspend fun clearRecentSearches(): Result<Unit>
}
