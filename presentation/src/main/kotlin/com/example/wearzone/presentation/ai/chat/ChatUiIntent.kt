package com.example.wearzone.presentation.ai.chat

sealed interface ChatUiIntent {
    data class OnInputTextChanged(val text: String) : ChatUiIntent
    data object OnSendMessage : ChatUiIntent
    data object OnClearHistory : ChatUiIntent
    data object OnDismissError : ChatUiIntent
}
