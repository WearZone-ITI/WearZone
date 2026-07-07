package com.example.wearzone.domain.ai.chat.usecase

import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.ai.chat.repository.IAiChatRepository
import kotlinx.coroutines.flow.Flow

class GetChatHistoryUseCase(
    private val repository: IAiChatRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> {
        return repository.getChatHistory()
    }
}
