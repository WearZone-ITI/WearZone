package com.example.wearzone.domain.ai.chat.model

enum class ChatRole {
    USER, MODEL, FUNCTION
}

data class ChatProductCard(
    val productId: String,
    val title: String,
    val imageUrl: String?,
    val price: Double?,
    val currencyCode: String,
    val vendor: String,
    val productType: String?,
    val reason: String,
    val isOutOfStock: Boolean? = null,
)

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val timestamp: Long,
    val isPending: Boolean = false,
    val products: List<ChatProductCard> = emptyList(),
)
