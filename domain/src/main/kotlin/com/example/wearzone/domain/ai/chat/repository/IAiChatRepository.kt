package com.example.wearzone.domain.ai.chat.repository

import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.common.DataResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IAiChatRepository {
    fun getChatHistory(): Flow<List<ChatMessage>>
    suspend fun clearHistory()
    suspend fun sendMessage(message: String): DataResult<Unit>
    suspend fun transcribeVoiceMessage(audioFile: File): DataResult<String>
}
