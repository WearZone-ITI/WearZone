package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.CustomerRequest
import com.example.wearzone.data.remote.dto.CustomerResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("admin/api/2024-04/customers.json")
    suspend fun createCustomer(
        @Body request: CustomerRequest
    ): CustomerResponse
}