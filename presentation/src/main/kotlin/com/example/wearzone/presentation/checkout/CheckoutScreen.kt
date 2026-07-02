package com.example.wearzone.presentation.checkout

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CheckoutUiEffect.NavigateBack -> onNavigateBack()
                CheckoutUiEffect.NavigateToOrderHistory -> onNavigateToOrderHistory()
                CheckoutUiEffect.ShowConfirmOrderDialog -> showConfirmDialog = true
                is CheckoutUiEffect.ShowMessage -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    CheckoutContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::handleIntent,
    )

    if (showConfirmDialog) {
        ConfirmOrderDialog(
            onConfirm = {
                showConfirmDialog = false
                viewModel.handleIntent(CheckoutUiIntent.OnSubmitOrderConfirmed)
            },
            onDismiss = { showConfirmDialog = false },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CheckoutContent(
    uiState: CheckoutUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (CheckoutUiIntent) -> Unit,
) {
    val content = uiState as? CheckoutUiState.Content

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.checkout_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(CheckoutUiIntent.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                ),
            )
        },
        bottomBar = {
            if (content != null) {
                CheckoutBottomBar(
                    state = content,
                    onSubmit = { onIntent(CheckoutUiIntent.OnSubmitOrderClicked) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppTheme.colors.background),
        ) {
            when (uiState) {
                CheckoutUiState.Loading -> CircularProgressIndicator(
                    color = AppTheme.colors.selected,
                    modifier = Modifier.align(Alignment.Center),
                )
                CheckoutUiState.Empty -> CheckoutEmptyContent()
                is CheckoutUiState.Error -> CheckoutErrorContent(
                    messageRes = uiState.messageRes,
                    onRetry = { onIntent(CheckoutUiIntent.OnRetry) },
                )
                is CheckoutUiState.Content -> CheckoutLoadedContent(uiState)
            }
        }
    }
}

@Composable
private fun CheckoutLoadedContent(
    state: CheckoutUiState.Content,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.checkout_review_order),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.cart_item_count_format, state.itemCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
        }
        items(state.items, key = { it.variantId }) { item ->
            CheckoutItemRow(item)
        }
        item { Spacer(modifier = Modifier.height(120.dp)) }
    }
}

@Composable
private fun CheckoutItemRow(item: CheckoutCartItemUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.checkout_quantity_format, item.quantity),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
        Text(
            text = item.formattedPrice,
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CheckoutBottomBar(
    state: CheckoutUiState.Content,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryRow(labelRes = R.string.cart_subtotal, value = state.subtotal)
        SummaryRow(labelRes = R.string.cart_total, value = state.total)
        Button(
            onClick = onSubmit,
            enabled = !state.isPlacingOrder,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.selected,
                contentColor = AppTheme.colors.onAccent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            if (state.isPlacingOrder) {
                CircularProgressIndicator(
                    color = AppTheme.colors.onAccent,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.checkout_place_order),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    labelRes: Int,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CheckoutEmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.checkout_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.checkout_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun CheckoutErrorContent(
    messageRes: Int,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.selected,
                contentColor = AppTheme.colors.onAccent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.cart_retry),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ConfirmOrderDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.checkout_confirm_title)) },
        text = { Text(text = stringResource(R.string.checkout_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.checkout_confirm_place_order),
                    color = AppTheme.colors.selected,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cart_dialog_cancel))
            }
        },
    )
}
