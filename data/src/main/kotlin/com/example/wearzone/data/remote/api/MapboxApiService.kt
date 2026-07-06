package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.MapboxGeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapboxApiService {
    @GET("geocoding/v5/mapbox.places/{query}.json")
    suspend fun searchPlaces(
        @Path("query") query: String,
        @Query("access_token") accessToken: String,
        @Query("autocomplete") autocomplete: Boolean = true,
        @Query("limit") limit: Int = 6,
        @Query("types") types: String = "address,poi,place,locality,neighborhood,district,region,postcode",
        @Query("country") countryIsoCode: String? = null,
    ): MapboxGeocodingResponseDto

    @GET("geocoding/v5/mapbox.places/{coordinates}.json")
    suspend fun reverseGeocode(
        @Path("coordinates") coordinates: String,
        @Query("access_token") accessToken: String,
        @Query("limit") limit: Int = 1,
        @Query("types") types: String = "address,poi,place,locality,neighborhood,district,region,postcode",
    ): MapboxGeocodingResponseDto
}
