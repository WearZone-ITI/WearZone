package com.example.wearzone.data.repository

import com.example.wearzone.data.local.ai.chat.ChatDao
import com.example.wearzone.data.local.ai.chat.ChatMessageEntity
import com.example.wearzone.data.remote.ai.chat.IAiChatRemoteDataSource
import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.ai.chat.model.ChatRole
import com.example.wearzone.domain.ai.chat.repository.IAiChatRepository
import com.example.wearzone.domain.common.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class AiChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: IAiChatRemoteDataSource,
    private val chatDao: ChatDao
) : IAiChatRepository {

    override fun getChatHistory(): Flow<List<ChatMessage>> {
        return chatDao.getAllMessages().map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    role = ChatRole.valueOf(entity.role),
                    content = entity.content,
                    timestamp = entity.timestamp,
                    isPending = entity.isPending
                )
            }
        }
    }

    override suspend fun clearHistory() {
        chatDao.clearHistory()
    }

    override suspend fun sendMessage(message: String): DataResult<Unit> {
        return try {
            val userMessage = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                role = ChatRole.USER.name,
                content = message,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(userMessage)
            
            // Generate response (could add a 'pending' message here for loading)
            val pendingMessageId = UUID.randomUUID().toString()
            chatDao.insertMessage(
                ChatMessageEntity(
                    id = pendingMessageId,
                    role = ChatRole.MODEL.name,
                    content = "...",
                    timestamp = System.currentTimeMillis(),
                    isPending = true
                )
            )

            // Get history to pass context
            val historyEntities = chatDao.getMessagesSync()
            val history = historyEntities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    role = ChatRole.valueOf(entity.role),
                    content = entity.content,
                    timestamp = entity.timestamp,
                    isPending = entity.isPending
                )
            }.filter { !it.isPending } // Ignore pending messages in history sent to remote
            
            val response = remoteDataSource.sendMessage(message, history)

            chatDao.insertMessage(
                ChatMessageEntity(
                    id = pendingMessageId,
                    role = ChatRole.MODEL.name,
                    content = response,
                    timestamp = System.currentTimeMillis(),
                    isPending = false
                )
            )
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(com.example.wearzone.domain.common.DomainError.Unknown(e))
        }
    }
}
