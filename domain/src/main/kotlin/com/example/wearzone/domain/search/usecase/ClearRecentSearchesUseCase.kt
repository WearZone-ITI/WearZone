package com.example.wearzone.domain.search.usecase

import com.example.wearzone.domain.search.repository.IRecentSearchRepository

class ClearRecentSearchesUseCase(
    private val repository: IRecentSearchRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.clearRecentSearches()
}
