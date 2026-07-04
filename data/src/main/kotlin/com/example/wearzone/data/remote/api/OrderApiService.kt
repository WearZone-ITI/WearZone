package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.OrdersResponseDto
import com.example.wearzone.data.remote.dto.ShopifyCancelOrderRequestDto
import com.example.wearzone.data.remote.dto.ShopifyOrderRequestDto
import com.example.wearzone.data.remote.dto.ShopifyOrderResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {
    @GET("admin/api/{version}/orders.json")
    suspend fun getOrders(
        @Path("version") version: String,
        @Query("status") status: String = "any",
        @Query("limit") limit: Int = 50,
        @Query("customer_id") customerId: Long,
    ): OrdersResponseDto

    @GET("admin/api/{version}/orders/{order_id}.json")
    suspend fun getOrder(
        @Path("version") version: String,
        @Path("order_id") orderId: Long,
    ): ShopifyOrderResponseDto

    @POST("admin/api/{version}/orders.json")
    suspend fun createOrder(
        @Path("version") version: String,
        @Body request: ShopifyOrderRequestDto,
    ): ShopifyOrderResponseDto

    @POST("admin/api/{version}/orders/{order_id}/cancel.json")
    suspend fun cancelOrder(
        @Path("version") version: String,
        @Path("order_id") orderId: Long,
        @Body request: ShopifyCancelOrderRequestDto,
    ): ShopifyOrderResponseDto
}
