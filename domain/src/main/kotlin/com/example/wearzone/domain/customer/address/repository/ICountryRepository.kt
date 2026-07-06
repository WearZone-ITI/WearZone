package com.example.wearzone.domain.customer.address.repository

import com.example.wearzone.domain.customer.address.model.Country

interface ICountryRepository {
    suspend fun getCountries(): Result<List<Country>>
}
