package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.CartApiService
import com.example.wearzone.data.remote.dto.DraftOrderRequest
import com.example.wearzone.data.remote.dto.DraftOrderResponse
import javax.inject.Inject

class CartRemoteDataSourceImpl @Inject constructor(
    private val cartApiService: CartApiService
) : ICartRemoteDataSource {

    override suspend fun createDraftOrder(request: DraftOrderRequest): DraftOrderResponse {
        return cartApiService.createDraftOrder(request)
    }

    override suspend fun updateDraftOrder(
        draftOrderId: Long,
        request: DraftOrderRequest
    ): DraftOrderResponse {
        return cartApiService.updateDraftOrder(draftOrderId, request)
    }

    override suspend fun getDraftOrder(draftOrderId: Long): DraftOrderResponse {
        return cartApiService.getDraftOrder(draftOrderId)
    }
    override suspend fun deleteDraftOrder(draftOrderId: Long) {
        cartApiService.deleteDraftOrder(draftOrderId)
    }
}
