package com.example.wearzone.domain.ai.chat.usecase

import com.example.wearzone.domain.ai.chat.repository.IAiChatRepository
import com.example.wearzone.domain.common.DataResult

class SendChatMessageUseCase(
    private val repository: IAiChatRepository
) {
    suspend operator fun invoke(message: String): DataResult<Unit> {
        return repository.sendMessage(message)
    }
}
