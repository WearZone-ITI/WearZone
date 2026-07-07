package com.example.wearzone.domain.ai.chat.usecase

import com.example.wearzone.domain.ai.chat.repository.IAiChatRepository

class ClearChatHistoryUseCase(
    private val repository: IAiChatRepository
) {
    suspend operator fun invoke() {
        repository.clearHistory()
    }
}
