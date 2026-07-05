package com.example.wearzone.data.repository

import com.example.wearzone.data.remote.ai.chat.IAiChatRemoteDataSource
import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.ai.chat.model.ChatRole
import com.example.wearzone.domain.ai.chat.repository.IAiChatRepository
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: IAiChatRemoteDataSource
) : IAiChatRepository {

    private val _history = MutableStateFlow<List<ChatMessage>>(emptyList())

    override fun getChatHistory(): Flow<List<ChatMessage>> {
        return _history.asStateFlow()
    }

    override suspend fun clearHistory() {
        _history.value = emptyList()
    }

    override suspend fun sendMessage(message: String): DataResult<Unit> {
        val userMsgId = UUID.randomUUID().toString()
        val userMessage = ChatMessage(
            id = userMsgId,
            role = ChatRole.USER,
            content = message,
            timestamp = System.currentTimeMillis()
        )

        val pendingMsgId = UUID.randomUUID().toString()
        val pendingMessage = ChatMessage(
            id = pendingMsgId,
            role = ChatRole.MODEL,
            content = "...",
            timestamp = System.currentTimeMillis(),
            isPending = true
        )

        // 1. Add user and pending messages to local history flow
        _history.value = _history.value + listOf(userMessage, pendingMessage)

        // 2. Fetch history context to pass to the API (excluding pending message)
        val historyToSend = _history.value.filter { it.id != pendingMsgId }

        return try {
            val response = remoteDataSource.sendMessage(message, historyToSend)

            // 3. Success: replace pending message with response
            _history.value = _history.value.map { msg ->
                if (msg.id == pendingMsgId) {
                    ChatMessage(
                        id = pendingMsgId,
                        role = ChatRole.MODEL,
                        content = response,
                        timestamp = System.currentTimeMillis(),
                        isPending = false
                    )
                } else {
                    msg
                }
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            // 4. Failure: remove the pending message, but don't add the error to history
            _history.value = _history.value.filter { it.id != pendingMsgId }
            DataResult.Error(DomainError.Unknown(e))
        }
    }
}
