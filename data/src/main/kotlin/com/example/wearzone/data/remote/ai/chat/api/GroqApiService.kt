package com.example.wearzone.data.remote.ai.chat.api

import com.example.wearzone.data.remote.ai.chat.dto.GroqRequest
import com.example.wearzone.data.remote.ai.chat.dto.GroqResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): Response<GroqResponse>
}

