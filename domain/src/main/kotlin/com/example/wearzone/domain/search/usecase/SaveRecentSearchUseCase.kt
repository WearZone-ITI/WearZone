package com.example.wearzone.domain.search.usecase

import com.example.wearzone.domain.search.repository.IRecentSearchRepository

class SaveRecentSearchUseCase(
    private val repository: IRecentSearchRepository,
) {
    suspend operator fun invoke(query: String): Result<Unit> = repository.saveRecentSearch(query)
}
