package com.example.wearzone.domain.search.usecase

import com.example.wearzone.domain.search.model.RecentSearch
import com.example.wearzone.domain.search.repository.IRecentSearchRepository
import kotlinx.coroutines.flow.Flow

class GetRecentSearchesUseCase(
    private val repository: IRecentSearchRepository,
) {
    operator fun invoke(): Flow<List<RecentSearch>> = repository.observeRecentSearches()
}
