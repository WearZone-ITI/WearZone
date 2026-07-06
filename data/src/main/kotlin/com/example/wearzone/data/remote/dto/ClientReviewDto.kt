package com.example.wearzone.data.remote.dto

import com.example.wearzone.domain.product.model.ClientReview
import com.google.firebase.firestore.DocumentSnapshot

data class ClientReviewDto(
    val id: String = "",
    val productId: String = "",
    val shopperName: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val timestamp: Long = 0L
) {
    fun toDomain(): ClientReview {
        return ClientReview(
            id = id,
            productId = productId,
            shopperName = shopperName,
            rating = rating,
            comment = comment,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDocument(snapshot: DocumentSnapshot): ClientReviewDto {
            val ratingVal = snapshot.get("rating")
            val rating = when (ratingVal) {
                is Double -> ratingVal
                is Long -> ratingVal.toDouble()
                is Float -> ratingVal.toDouble()
                is Int -> ratingVal.toDouble()
                else -> 0.0
            }

            val timestampVal = snapshot.get("timestamp")
            val timestamp = when (timestampVal) {
                is Long -> timestampVal
                is Int -> timestampVal.toLong()
                is Double -> timestampVal.toLong()
                else -> 0L
            }

            return ClientReviewDto(
                id = snapshot.id,
                productId = snapshot.getString("productId") ?: "",
                shopperName = snapshot.getString("shopperName") ?: "",
                rating = rating,
                comment = snapshot.getString("comment") ?: "",
                timestamp = timestamp
            )
        }

        fun toMap(productId: String, shopperName: String, rating: Double, comment: String, timestamp: Long): Map<String, Any> {
            return mapOf(
                "productId" to productId,
                "shopperName" to shopperName,
                "rating" to rating,
                "comment" to comment,
                "timestamp" to timestamp
            )
        }
    }
}
