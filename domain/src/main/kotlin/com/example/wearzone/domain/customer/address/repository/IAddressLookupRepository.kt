package com.example.wearzone.domain.customer.address.repository

import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.model.AddressSuggestion

interface IAddressLookupRepository {
    suspend fun searchAddressSuggestions(
        query: String,
        countryIsoCode: String?,
    ): Result<List<AddressSuggestion>>

    suspend fun reverseGeocode(coordinates: AddressCoordinates): Result<AddressSuggestion?>
}
