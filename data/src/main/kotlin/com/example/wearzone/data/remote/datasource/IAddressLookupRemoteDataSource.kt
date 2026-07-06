package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.dto.MapboxFeatureDto

interface IAddressLookupRemoteDataSource {
    suspend fun searchAddressSuggestions(
        query: String,
        accessToken: String,
        countryIsoCode: String?,
    ): List<MapboxFeatureDto>

    suspend fun reverseGeocode(
        longitude: Double,
        latitude: Double,
        accessToken: String,
    ): MapboxFeatureDto?
}
