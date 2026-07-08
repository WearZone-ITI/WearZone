package com.example.wearzone.domain.ai.chat.usecase

import com.example.wearzone.domain.ai.chat.repository.IAiChatRepository
import com.example.wearzone.domain.common.DataResult
import java.io.File
import javax.inject.Inject

class TranscribeVoiceUseCase @Inject constructor(
    private val repository: IAiChatRepository
) {
    suspend operator fun invoke(file: File): DataResult<String> {
        return repository.transcribeVoiceMessage(file)
    }
}
