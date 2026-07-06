package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.Country
import com.example.wearzone.domain.customer.address.repository.ICountryRepository

class GetCountriesUseCase(
    private val repository: ICountryRepository,
) {
    suspend operator fun invoke(): Result<List<Country>> =
        repository.getCountries()
}
