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
    val rawText: String,
    val timestamp: Long,
    val isPending: Boolean = false,
    val products: ImmutableList<ChatUiProductCard> = persistentListOf(),
)

data class ChatUiProductCard(
    val productId: String,
    val title: String,
    val imageUrl: String?,
    val price: Double?,
    val currencyCode: String,
    val vendor: String,
    val productType: String?,
    val reason: String,
    val isOutOfStock: Boolean?,
)
