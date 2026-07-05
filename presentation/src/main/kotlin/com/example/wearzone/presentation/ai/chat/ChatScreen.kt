package com.example.wearzone.presentation.ai.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

// ─── VogueVibe AI Colors ─────────────────────────────────────────────────────

private val AiGradientStart = Color(0xFF6C3DE8)
private val AiGradientEnd   = Color(0xFFB06AE8)
private val UserBubble      = Color(0xFF6C3DE8)
private val AiBubble        = Color(0xFFF3EEFF)
private val AiBubbleText    = Color(0xFF1C1040)
private val InputBackground = Color(0xFFF8F5FF)

// ─── Screen entry ─────────────────────────────────────────────────────────────

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

// ─── Main content ─────────────────────────────────────────────────────────────

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
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(AiGradientStart, AiGradientEnd)
                            )
                        )
                ) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "WearZone AI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Your personal stylist",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            if (uiState.messages.isNotEmpty()) {
                                IconButton(onClick = { onIntent(ChatUiIntent.OnClearHistory) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Clear chat",
                                        tint = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    )
                }
            }
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        if (message.isFromUser) {
                            UserMessageBubble(message.text)
                        } else {
                            Column {
                                AiMessageBubble(message.text, message.isPending)
                                if (!message.isPending) {
                                    val products = remember(message.text) { parseProductsFromMessage(message.text) }
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
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(listOf(AiGradientStart, AiGradientEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Hi! I'm WearZone AI ✨",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AiGradientStart
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Ask me anything about fashion,\nproducts, or style recommendations.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        SuggestionChips()
    }
}

@Composable
private fun SuggestionChips() {
    val suggestions = listOf("Show me shirts", "Best sellers", "Summer dresses", "Latest sneakers")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AiBubble,
                        modifier = Modifier
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = AiGradientStart,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─── Message bubbles ──────────────────────────────────────────────────────────

@Composable
fun UserMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
                )
                .clip(RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp))
                .background(
                    Brush.linearGradient(listOf(AiGradientStart, AiGradientEnd))
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
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
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar dot
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(listOf(AiGradientStart, AiGradientEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
                )
                .clip(RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                .background(AiBubble)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (isPending) {
                TypingIndicator()
            } else {
                Text(
                    text = text,
                    color = AiBubbleText,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// ─── Animated typing indicator ────────────────────────────────────────────────

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

    val dot1 = animatedDot(0)
    val dot2 = animatedDot(120)
    val dot3 = animatedDot(240)

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(24.dp)
    ) {
        listOf(dot1, dot2, dot3).forEach { offset ->
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .offset(y = offset.dp)
                    .clip(CircleShape)
                    .background(AiGradientStart.copy(alpha = 0.6f))
            )
        }
    }
}

// ─── Input bar ────────────────────────────────────────────────────────────────

@Composable
private fun ChatInputBar(
    inputText: String,
    isSending: Boolean,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Ask about fashion...", color = Color.LightGray)
                },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AiGradientStart,
                    unfocusedBorderColor = Color(0xFFE0D8F8),
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { if (inputText.isNotBlank() && !isSending) onSendClicked() }
                )
            )
            Spacer(Modifier.width(10.dp))

            val canSend = inputText.isNotBlank() && !isSending
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSend)
                            Brush.linearGradient(listOf(AiGradientStart, AiGradientEnd))
                        else
                            Brush.linearGradient(listOf(Color.LightGray, Color.LightGray))
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSendClicked,
                    enabled = canSend,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─── Error Banner & Product Parsing/Rendering Helpers ──────────────────────────

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFFDE8E8),
        contentColor = Color(0xFF9B1C1C)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color(0xFFE53E3E),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF9B1C1C),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

data class ParsedProduct(
    val id: String,
    val title: String,
    val price: String
)

fun parseProductsFromMessage(text: String): List<ParsedProduct> {
    val idRegex = Regex("""(?:Product\s+)?ID:\s*(\d+)""", RegexOption.IGNORE_CASE)
    val idMatches = idRegex.findAll(text).toList()
    if (idMatches.isEmpty()) return emptyList()

    val titleRegex = Regex("""\*\*(.*?)\*\*""")
    val priceRegex = Regex(
        """(?:\$|EGP|USD|LE|€|£)\s*\d+(?:\.\d+)?|\d+(?:\.\d+)?\s*(?:EGP|USD|LE|\$|€|£)""",
        RegexOption.IGNORE_CASE
    )

    if (idMatches.size == 1) {
        val id = idMatches[0].groupValues[1]
        val title = titleRegex.find(text)?.groupValues?.get(1) ?: "Product Info"
        val price = priceRegex.find(text)?.value ?: ""
        return listOf(ParsedProduct(id = id, title = title, price = price))
    }

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
            
        parsedProducts.add(ParsedProduct(id = id, title = title, price = price))
        lastIndex = endIndex
    }
    return parsedProducts
}

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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = R.drawable.placeholder,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (product.price.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.price,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: ${product.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        }
    }
}
