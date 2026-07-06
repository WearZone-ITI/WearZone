package com.example.wearzone.presentation.ai.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

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
    snackbarHostState: SnackbarHostState
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
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
                onSendClicked = { onIntent(ChatUiIntent.OnSendMessage) }
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
                                if (!message.isPending) {
                                    val products = remember(message.rawText) { parseProductsFromMessage(message.rawText) }
                                    if (products.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(
                                            modifier = Modifier.padding(start = 36.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            products.forEach { product ->
                                                ProductCardInChat(
                                                    product = product,
                                                    onClick = { onNavigateToProductDetail(product.id) }
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
}

// ─── Welcome / empty state ────────────────────────────────────────────────────

@Composable
private fun WelcomeState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AppTheme.colors.accent,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.welcome_ai_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.welcome_ai_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

// ─── Message bubbles ──────────────────────────────────────────────────────────

@Composable
fun UserMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp),
            color = AppTheme.colors.selected,
            contentColor = AppTheme.colors.onAccent
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AiMessageBubble(text: String, isPending: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AppTheme.colors.accent,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
            color = AppTheme.colors.surfaceVariant,
            contentColor = AppTheme.colors.textPrimary
        ) {
            if (isPending) {
                Box(modifier = Modifier.padding(16.dp)) { TypingIndicator() }
            } else {
                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    @Composable
    fun animatedDot(delayMs: Int): Float {
        val anim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -8f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, delayMs, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot"
        )
        return anim
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(0, 120, 240).forEach { delay ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = animatedDot(delay).dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accent)
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    isSending: Boolean,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit
) {
    Surface(
        color = AppTheme.colors.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(stringResource(R.string.ask_fashion_hint), color = AppTheme.colors.textSecondary)
                },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.accent,
                    unfocusedBorderColor = AppTheme.colors.divider,
                    focusedContainerColor = AppTheme.colors.surfaceVariant,
                    unfocusedContainerColor = AppTheme.colors.surfaceVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { if (inputText.isNotBlank() && !isSending) onSendClicked() }
                )
            )
            Spacer(Modifier.width(12.dp))
            val canSend = inputText.isNotBlank() && !isSending
            IconButton(
                onClick = onSendClicked,
                enabled = canSend,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (canSend) AppTheme.colors.selected else AppTheme.colors.divider)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send),
                    tint = if (canSend) AppTheme.colors.onAccent else AppTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.error.copy(alpha = 0.1f),
        contentColor = AppTheme.colors.error
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

data class ParsedProduct(
    val id: String,
    val title: String,
    val price: String,
    val imageUrl: String
)

@Composable
fun ProductCardInChat(
    product: ParsedProduct,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 280.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        border = BorderStroke(1.dp, AppTheme.colors.divider)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.colors.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (product.price.isNotEmpty()) {
                    Text(
                        text = product.price,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.accent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun parseProductsFromMessage(text: String): List<ParsedProduct> {
    val idRegex = Regex("""\[ID:\s*(\d+)\]""", RegexOption.IGNORE_CASE)
    val idMatches = idRegex.findAll(text).toList()
    if (idMatches.isEmpty()) return emptyList()

    val titleRegex = Regex("""\*\*(.*?)\*\*""")
    val priceRegex = Regex(
        """(?:\$|EGP|USD|LE|€|£)\s*\d+(?:\.\d+)?|\d+(?:\.\d+)?\s*(?:EGP|USD|LE|\$|€|£)""",
        RegexOption.IGNORE_CASE
    )
    val imageUrlRegex = Regex("""image_url":\s*"(.*?)"""")

    val parsedProducts = mutableListOf<ParsedProduct>()
    var lastIndex = 0
    for (i in idMatches.indices) {
        val idMatch = idMatches[i]
        val id = idMatch.groupValues[1]
        val endIndex = idMatch.range.last + 1
        val segment = text.substring(lastIndex, endIndex)

        val title = titleRegex.find(segment)?.groupValues?.get(1)
            ?: titleRegex.find(text.substring(idMatch.range.first))?.groupValues?.get(1)
            ?: "Product Info"

        val price = priceRegex.find(segment)?.value
            ?: priceRegex.find(text.substring(idMatch.range.first))?.value
            ?: ""

        val imageUrl = imageUrlRegex.find(text.substring(idMatch.range.first))?.groupValues?.get(1) ?: ""

        parsedProducts.add(ParsedProduct(id = id, title = title, price = price, imageUrl = imageUrl))
        lastIndex = endIndex
    }
    return parsedProducts
}
