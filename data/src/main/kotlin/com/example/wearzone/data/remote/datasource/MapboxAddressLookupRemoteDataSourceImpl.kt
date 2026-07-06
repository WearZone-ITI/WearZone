package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.MapboxApiService
import com.example.wearzone.data.remote.dto.MapboxFeatureDto
import javax.inject.Inject

class MapboxAddressLookupRemoteDataSourceImpl @Inject constructor(
    private val apiService: MapboxApiService,
) : IAddressLookupRemoteDataSource {
    override suspend fun searchAddressSuggestions(
        query: String,
        accessToken: String,
        countryIsoCode: String?,
    ): List<MapboxFeatureDto> =
        apiService.searchPlaces(
            query = query,
            accessToken = accessToken,
            countryIsoCode = countryIsoCode?.lowercase(),
        ).features

    override suspend fun reverseGeocode(
        longitude: Double,
        latitude: Double,
        accessToken: String,
    ): MapboxFeatureDto? =
        apiService.reverseGeocode(
            coordinates = "$longitude,$latitude",
            accessToken = accessToken,
        ).features.firstOrNull()
}
