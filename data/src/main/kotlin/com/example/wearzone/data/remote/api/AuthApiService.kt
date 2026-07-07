package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.CustomerRequest
import com.example.wearzone.data.remote.dto.CustomerResponse
import com.example.wearzone.data.remote.dto.CustomersResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("admin/api/2024-04/customers.json")
    suspend fun createCustomer(
        @Body request: CustomerRequest
    ): CustomerResponse

    @GET("admin/api/2024-04/customers/search.json")
    suspend fun searchCustomers(
        @Query("query") query: String,
    ): CustomersResponse
}