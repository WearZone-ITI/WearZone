package com.example.wearzone.presentation.ai.chat

sealed interface ChatUiEffect {
    data class ShowSnackbar(val message: String) : ChatUiEffect
    data object NavigateBack : ChatUiEffect
}
