package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.AddressSuggestion
import com.example.wearzone.domain.customer.address.repository.IAddressLookupRepository

class SearchAddressSuggestionsUseCase(
    private val repository: IAddressLookupRepository,
) {
    suspend operator fun invoke(
        query: String,
        countryIsoCode: String?,
    ): Result<List<AddressSuggestion>> =
        repository.searchAddressSuggestions(query, countryIsoCode)
}
