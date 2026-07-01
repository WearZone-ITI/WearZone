package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.DraftOrderRequest
import com.example.wearzone.data.remote.dto.DraftOrderResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApiService {

    @POST("admin/api/2024-04/draft_orders.json")
    suspend fun createDraftOrder(@Body request: DraftOrderRequest): DraftOrderResponse

    @PUT("admin/api/2024-04/draft_orders/{draft_order_id}.json")
    suspend fun updateDraftOrder(
        @Path("draft_order_id") draftOrderId: Long,
        @Body request: DraftOrderRequest
    ): DraftOrderResponse

    @GET("admin/api/2024-04/draft_orders/{draft_order_id}.json")
    suspend fun getDraftOrder(
        @Path("draft_order_id") draftOrderId: Long
    ): DraftOrderResponse

    @DELETE("draft_orders/{id}.json")
    suspend fun deleteDraftOrder(
        @Path("id")
        draftOrderId: Long,
    )
}
