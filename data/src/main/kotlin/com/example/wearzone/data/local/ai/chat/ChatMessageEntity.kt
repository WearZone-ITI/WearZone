package com.example.wearzone.data.local.ai.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val isPending: Boolean = false
)
