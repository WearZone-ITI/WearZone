package com.example.wearzone.presentation.ai.chat

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ChatUiState(
    val messages: ImmutableList<ChatUiMessage> = persistentListOf(),
    val isSending: Boolean = false,
    val inputText: String = "",
    val error: String? = null
)

data class ChatUiMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String,
    val rawText: String, // Store raw text for product parsing
    val timestamp: Long,
    val isPending: Boolean = false
)
