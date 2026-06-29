package com.example.wearzone.data.repository.search

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.search.RecentSearchDao
import com.example.wearzone.data.local.search.RecentSearchDto
import com.example.wearzone.domain.search.model.RecentSearch
import com.example.wearzone.domain.search.repository.IRecentSearchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecentSearchRepositoryImpl @Inject constructor(
    private val dao: RecentSearchDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IRecentSearchRepository {

    override fun observeRecentSearches(): Flow<List<RecentSearch>> {
        return dao.observeRecentSearches()
            .map { searches -> searches.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun saveRecentSearch(query: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val normalizedQuery = query.trim()
            if (normalizedQuery.isNotBlank()) {
                dao.upsertRecentSearch(
                    RecentSearchDto(
                        query = normalizedQuery,
                        searchedAt = System.currentTimeMillis(),
                    )
                )
                dao.pruneOldSearches()
            }
        }
    }

    override suspend fun clearRecentSearches(): Result<Unit> = withContext(ioDispatcher) {
        runCatching { dao.clearRecentSearches() }
    }
}
