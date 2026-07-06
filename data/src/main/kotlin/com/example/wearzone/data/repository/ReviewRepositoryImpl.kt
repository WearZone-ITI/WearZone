package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.dto.ClientReviewDto
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.model.ClientReview
import com.example.wearzone.domain.product.repository.IReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IReviewRepository {

    override fun getReviewsForProduct(productId: String): Flow<List<ClientReview>> = callbackFlow {
        val query = firestore.collection("reviews")
            .whereEqualTo("productId", productId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // If there's an error, log it or close the channel
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val reviews = snapshot.documents.map { doc ->
                    ClientReviewDto.fromDocument(doc).toDomain()
                }
                trySend(reviews)
            }
        }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    override suspend fun addReview(
        productId: String,
        shopperName: String,
        rating: Double,
        comment: String
    ): DataResult<Unit> = withContext(ioDispatcher) {
        try {
            val data = ClientReviewDto.toMap(
                productId = productId,
                shopperName = shopperName,
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
            firestore.collection("reviews").add(data).await()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(DomainError.Unknown(e))
        }
    }
}
