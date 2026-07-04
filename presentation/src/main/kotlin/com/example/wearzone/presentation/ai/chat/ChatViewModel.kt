package com.example.wearzone.presentation.ai.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.ai.chat.model.ChatRole
import com.example.wearzone.domain.ai.chat.usecase.ClearChatHistoryUseCase
import com.example.wearzone.domain.ai.chat.usecase.GetChatHistoryUseCase
import com.example.wearzone.domain.ai.chat.usecase.SendChatMessageUseCase
import com.example.wearzone.domain.common.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ChatUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        observeChatHistory()
    }

    private fun observeChatHistory() {
        viewModelScope.launch {
            getChatHistoryUseCase().collectLatest { messages ->
                val uiMessages = messages.map { msg ->
                    ChatUiMessage(
                        id = msg.id,
                        isFromUser = msg.role == ChatRole.USER,
                        text = msg.content,
                        timestamp = msg.timestamp,
                        isPending = msg.isPending
                    )
                }.toImmutableList()
                _uiState.update { it.copy(messages = uiMessages) }
            }
        }
    }

    fun handleIntent(intent: ChatUiIntent) {
        when (intent) {
            is ChatUiIntent.OnInputTextChanged -> _uiState.update { it.copy(inputText = intent.text) }
            ChatUiIntent.OnSendMessage -> sendMessage()
            ChatUiIntent.OnClearHistory -> clearHistory()
            ChatUiIntent.OnDismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun sendMessage() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return
        
        _uiState.update { it.copy(inputText = "", isSending = true) }
        
        viewModelScope.launch {
            val result = sendChatMessageUseCase(currentText)
            _uiState.update { it.copy(isSending = false) }
            if (result is DataResult.Error) {
                val errorMsg = when (val err = result.error) {
                    is com.example.wearzone.domain.common.DomainError.Network -> err.exception.message
                    is com.example.wearzone.domain.common.DomainError.Server -> err.message
                    is com.example.wearzone.domain.common.DomainError.Unknown -> err.exception.message
                } ?: "Failed to send message"
                _uiState.update { it.copy(error = errorMsg) }
                _uiEffect.send(ChatUiEffect.ShowSnackbar(errorMsg))
            }
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            clearChatHistoryUseCase()
        }
    }
}
