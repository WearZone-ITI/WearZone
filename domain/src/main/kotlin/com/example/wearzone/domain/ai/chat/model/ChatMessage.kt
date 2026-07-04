package com.example.wearzone.domain.ai.chat.model

enum class ChatRole {
    USER, MODEL, FUNCTION
}

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val timestamp: Long,
    val isPending: Boolean = false
)
