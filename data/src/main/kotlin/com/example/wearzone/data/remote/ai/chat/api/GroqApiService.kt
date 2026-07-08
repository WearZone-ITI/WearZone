package com.example.wearzone.data.remote.ai.chat.api

import com.example.wearzone.data.remote.ai.chat.dto.GroqRequest
import com.example.wearzone.data.remote.ai.chat.dto.GroqResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GroqApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): Response<GroqResponse>

    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody // Reverted back to RequestBody
    ): WhisperResponse
}

data class WhisperResponse(val text: String)

