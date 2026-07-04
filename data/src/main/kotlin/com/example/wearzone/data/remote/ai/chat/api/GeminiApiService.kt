package com.example.wearzone.data.remote.ai.chat.api

import com.example.wearzone.data.remote.ai.chat.dto.GeminiRequest
import com.example.wearzone.data.remote.ai.chat.dto.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface GeminiApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
