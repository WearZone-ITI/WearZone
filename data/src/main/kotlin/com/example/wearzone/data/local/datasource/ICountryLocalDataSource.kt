package com.example.wearzone.data.local.datasource

import com.example.wearzone.domain.customer.address.model.Country

interface ICountryLocalDataSource {
    fun getCountries(): List<Country>
}
