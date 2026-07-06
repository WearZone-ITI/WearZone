package com.example.wearzone.domain.product.repository

import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.ClientReview
import kotlinx.coroutines.flow.Flow

interface IReviewRepository {
    fun getReviewsForProduct(productId: String): Flow<List<ClientReview>>
    suspend fun addReview(
        productId: String,
        shopperName: String,
        rating: Double,
        comment: String
    ): DataResult<Unit>
}
