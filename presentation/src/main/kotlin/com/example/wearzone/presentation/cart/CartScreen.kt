package com.example.wearzone.presentation.cart

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.cart.components.CartItemRow
import com.example.wearzone.presentation.cart.components.EmptyCartContent
import com.example.wearzone.presentation.cart.components.PriceSummaryBar
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCheckout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingRemoveVariantId by remember { mutableStateOf<String?>(null) }
    var showClearCartDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CartUiEffect.NavigateToLogin -> onNavigateToLogin()
                CartUiEffect.NavigateToCheckout -> onNavigateToCheckout()
                CartUiEffect.ShowClearCartConfirmation -> showClearCartDialog = true
                is CartUiEffect.ShowRemoveConfirmation -> pendingRemoveVariantId = effect.variantId
                is CartUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CartContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::handleIntent,
    )

    pendingRemoveVariantId?.let { variantId ->
        CartConfirmationDialog(
            title = stringResource(id = R.string.cart_remove_dialog_title),
            message = stringResource(id = R.string.cart_remove_dialog_message),
            confirmLabel = stringResource(id = R.string.cart_remove_dialog_confirm),
            onConfirm = {
                pendingRemoveVariantId = null
                viewModel.handleIntent(CartUiIntent.OnRemoveItemConfirmed(variantId))
            },
            onDismiss = { pendingRemoveVariantId = null },
        )
    }

    if (showClearCartDialog) {
        CartConfirmationDialog(
            title = stringResource(id = R.string.cart_clear_dialog_title),
            message = stringResource(id = R.string.cart_clear_dialog_message),
            confirmLabel = stringResource(id = R.string.cart_clear_dialog_confirm),
            onConfirm = {
                showClearCartDialog = false
                viewModel.handleIntent(CartUiIntent.OnClearCartConfirmed)
            },
            onDismiss = { showClearCartDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartContent(
    uiState: CartUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onIntent: (CartUiIntent) -> Unit,
) {
    val content = uiState as? CartUiState.Content

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = AppTheme.colors.textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.cart_back_content_description),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                actions = {
                    if (content != null) {
                        IconButton(onClick = { onIntent(CartUiIntent.OnClearCartClicked) }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = stringResource(id = R.string.cart_clear_content_description),
                                tint = AppTheme.colors.textSecondary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background),
            )
        },
        bottomBar = {
            if (content != null) {
                PriceSummaryBar(
                    subtotal = content.subtotal,
                    total = content.total,
                    onCheckout = { onIntent(CartUiIntent.OnCheckoutClicked) },
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AppTheme.colors.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppTheme.colors.background),
        ) {
            when (uiState) {
                CartUiState.Loading -> CircularProgressIndicator(
                    color = AppTheme.colors.selected,
                    modifier = Modifier.align(Alignment.Center),
                )
                CartUiState.LoginRequired -> Text(
                    text = stringResource(id = R.string.cart_login_required),
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.align(Alignment.Center),
                )
                CartUiState.Empty -> EmptyCartContent(
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
                is CartUiState.Error -> CartErrorContent(
                    message = uiState.message,
                    onRetry = { onIntent(CartUiIntent.OnRetry) },
                    modifier = Modifier.align(Alignment.Center),
                )
                is CartUiState.Content -> CartItemsContent(
                    state = uiState,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun CartItemsContent(
    state: CartUiState.Content,
    onIntent: (CartUiIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(id = R.string.cart_title),
                    color = AppTheme.colors.textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = R.string.cart_item_count_format, state.itemCount),
                    color = AppTheme.colors.textSecondary,
                    fontSize = 16.sp,
                )
            }
        }
        items(state.items, key = { it.variantId }) { item ->
            CartItemRow(
                item = item,
                onIncrease = { onIntent(CartUiIntent.OnIncreaseQuantity(item.variantId)) },
                onDecrease = { onIntent(CartUiIntent.OnDecreaseQuantity(item.variantId)) },
                onRemove = { onIntent(CartUiIntent.OnRemoveItemClicked(item.variantId)) },
            )
        }
        item { Spacer(modifier = Modifier.height(120.dp)) }
    }
}

@Composable
private fun CartErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = AppTheme.colors.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRetry) {
            Text(text = stringResource(id = R.string.cart_retry))
        }
    }
}

@Composable
private fun CartConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmLabel, color = AppTheme.colors.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cart_dialog_cancel))
            }
        },
    )
}
