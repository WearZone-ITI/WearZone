package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.CurrencyResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApiService {
    @GET("v6/latest/{baseCode}")
    suspend fun getLatestRates(@Path("baseCode") baseCode: String): CurrencyResponseDto
}
