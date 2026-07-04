package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.PayMockPaymentRequestDto
import com.example.wearzone.data.remote.dto.PayMockPaymentResponseDto
import com.example.wearzone.data.remote.dto.PayMockProjectRequestDto
import com.example.wearzone.data.remote.dto.PayMockProjectResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PayMockApiService {
    @POST("projects")
    suspend fun createProject(
        @Body request: PayMockProjectRequestDto,
    ): PayMockProjectResponseDto

    @POST("payments")
    suspend fun processPayment(
        @Body request: PayMockPaymentRequestDto,
    ): PayMockPaymentResponseDto
}