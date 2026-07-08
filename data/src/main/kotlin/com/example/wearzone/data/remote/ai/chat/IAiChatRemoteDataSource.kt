package com.example.wearzone.data.remote.ai.chat

import com.example.wearzone.domain.ai.chat.model.ChatMessage
import java.io.File

interface IAiChatRemoteDataSource {
    suspend fun sendMessage(
        message: String,
        history: List<ChatMessage>,
        intent: String,
        catalogContext: String,
    ): String

    suspend fun transcribeAudio(file: File): String
}
