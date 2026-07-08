package com.example.wearzone.presentation.ai.chat

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.ai.chat.composable.AiMessageBubble
import com.example.wearzone.presentation.ai.chat.composable.ErrorBanner
import com.example.wearzone.presentation.ai.chat.composable.ProductCardInChat
import com.example.wearzone.presentation.ai.chat.composable.UserMessageBubble
import com.example.wearzone.presentation.ai.chat.composable.WelcomeState
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.unit.dp
import com.example.wearzone.presentation.ai.chat.composable.ChatInputBar
import java.io.File

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ChatUiEffect.NavigateBack -> onNavigateBack()
                is ChatUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ChatContent(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onNavigateBack = onNavigateBack,
        onNavigateToProductDetail = onNavigateToProductDetail,
        onTranscribeAudio = viewModel::transcribeAudio,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    uiState: ChatUiState,
    onIntent: (ChatUiIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onTranscribeAudio: (File) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val listState = rememberLazyListState()

    val renderedCardCount = uiState.messages.sumOf { it.products.size }
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.scrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface,
                    titleContentColor = AppTheme.colors.textPrimary,
                    navigationIconContentColor = AppTheme.colors.textPrimary,
                    actionIconContentColor = AppTheme.colors.textPrimary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AppTheme.colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.wearzone_ai),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.stylist_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (uiState.messages.isNotEmpty()) {
                        IconButton(onClick = { onIntent(ChatUiIntent.OnClearHistory) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.clear_chat),
                                tint = AppTheme.colors.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = uiState.inputText,
                isSending = uiState.isSending,
                onTextChanged = { onIntent(ChatUiIntent.OnInputTextChanged(it)) },
                onSendClicked = { onIntent(ChatUiIntent.OnSendMessage) },
                onTranscribeAudio = onTranscribeAudio
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.error != null) {
                ErrorBanner(
                    message = uiState.error,
                    onDismiss = { onIntent(ChatUiIntent.OnDismissError) }
                )
            }

            if (uiState.messages.isEmpty()) {
                WelcomeState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        if (message.isFromUser) {
                            UserMessageBubble(message.text)
                        } else {
                            Column {
                                AiMessageBubble(message.text, message.isPending)
                                if (!message.isPending && message.products.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(
                                        modifier = Modifier.padding(start = 36.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        message.products.forEach { product ->
                                            ProductCardInChat(
                                                product = product,
                                                onClick = { onNavigateToProductDetail(product.productId) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}