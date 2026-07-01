package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.CustomerAddressRequestDto
import com.example.wearzone.data.remote.dto.CustomerAddressResponseDto
import com.example.wearzone.data.remote.dto.CustomerAddressesResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AddressApiService {
    @GET("admin/api/{version}/customers/{customerId}/addresses.json")
    suspend fun getAddresses(
        @Path("version") version: String,
        @Path("customerId") customerId: Long,
        @Query("limit") limit: Int = 50,
    ): CustomerAddressesResponseDto

    @GET("admin/api/{version}/customers/{customerId}/addresses/{addressId}.json")
    suspend fun getAddress(
        @Path("version") version: String,
        @Path("customerId") customerId: Long,
        @Path("addressId") addressId: Long,
    ): CustomerAddressResponseDto

    @POST("admin/api/{version}/customers/{customerId}/addresses.json")
    suspend fun createAddress(
        @Path("version") version: String,
        @Path("customerId") customerId: Long,
        @Body request: CustomerAddressRequestDto,
    ): CustomerAddressResponseDto

    @PUT("admin/api/{version}/customers/{customerId}/addresses/{addressId}.json")
    suspend fun updateAddress(
        @Path("version") version: String,
        @Path("customerId") customerId: Long,
        @Path("addressId") addressId: Long,
        @Body request: CustomerAddressRequestDto,
    ): CustomerAddressResponseDto

    @PUT("admin/api/{version}/customers/{customerId}/addresses/{addressId}/default.json")
    suspend fun setDefaultAddress(
        @Path("version") version: String,
        @Path("customerId") customerId: Long,
        @Path("addressId") addressId: Long,
    ): CustomerAddressResponseDto

    @DELETE("admin/api/{version}/customers/{customerId}/addresses/{addressId}.json")
    suspend fun deleteAddress(
        @Path("version") version: String,
        @Path("customerId") customerId: Long,
        @Path("addressId") addressId: Long,
    ): Response<Unit>
}
