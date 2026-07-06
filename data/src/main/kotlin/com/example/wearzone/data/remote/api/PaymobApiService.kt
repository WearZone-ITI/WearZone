package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PaymobApiService {
    @POST("v1/intention/")
    suspend fun createIntention(
        @Header("Authorization") authHeader: String,
        @Body request: PaymobIntentionRequest
    ): PaymobIntentionResponse
}