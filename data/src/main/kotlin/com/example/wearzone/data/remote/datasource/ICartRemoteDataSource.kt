package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.dto.DraftOrderRequest
import com.example.wearzone.data.remote.dto.DraftOrderResponse

interface ICartRemoteDataSource {
    suspend fun createDraftOrder(request: DraftOrderRequest): DraftOrderResponse
    suspend fun updateDraftOrder(draftOrderId: Long, request: DraftOrderRequest): DraftOrderResponse
    suspend fun getDraftOrder(draftOrderId: Long): DraftOrderResponse
    suspend fun deleteDraftOrder(draftOrderId: Long)
}