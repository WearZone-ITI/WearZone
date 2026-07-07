package com.example.wearzone.presentation.order.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.wearzone.presentation.common.OrderListSkeleton
import com.example.wearzone.presentation.common.PremiumEmptyState
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.wearzone.presentation.common.SignInRequiredDialog
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.order.history.components.OrderHistoryCard
import kotlinx.coroutines.launch

@Composable
fun OrderHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: OrderHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                OrderHistoryUiEffect.NavigateBack -> onNavigateBack()
                is OrderHistoryUiEffect.NavigateToDetails -> onNavigateToDetails(effect.orderId)
                is OrderHistoryUiEffect.ShowMessage -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            OrderHistoryTopBar(onIntent = viewModel::handleIntent)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        OrderHistoryContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister,
            onContinueBrowsing = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun OrderHistoryContent(
    uiState: OrderHistoryUiState,
    onIntent: (OrderHistoryUiIntent) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueBrowsing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        when (uiState) {
            OrderHistoryUiState.Loading -> OrderListSkeleton()

            OrderHistoryUiState.Empty -> PremiumEmptyState(
                lottieResId = com.example.presentation.R.raw.cart_is_empty,
                title = stringResource(R.string.order_history_empty_title),
                description = stringResource(R.string.order_history_empty_subtitle),
                modifier = Modifier.fillMaxSize()
            )

            OrderHistoryUiState.SignInRequired -> SignInRequiredDialog(
                onSignInRegister = onNavigateToLogin,
                onContinueBrowsing = onContinueBrowsing,
            )

            is OrderHistoryUiState.Error -> OrderHistoryErrorContent(
                messageRes = uiState.messageRes,
                onRetry = { onIntent(OrderHistoryUiIntent.OnRetry) },
            )

            is OrderHistoryUiState.Content -> OrderHistoryLoadedContent(
                uiState = uiState,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OrderHistoryTopBar(
    onIntent: (OrderHistoryUiIntent) -> Unit,
) {
    TopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Text(
                text = stringResource(R.string.order_history_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = { onIntent(OrderHistoryUiIntent.OnBackClicked) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back),
                    tint = AppTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        actions = {
            IconButton(onClick = { onIntent(OrderHistoryUiIntent.OnRefresh) }) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.order_history_refresh),
                    tint = AppTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.background,
        ),
    )
}

@Composable
private fun OrderHistoryLoadedContent(
    uiState: OrderHistoryUiState.Content,
    onIntent: (OrderHistoryUiIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            OrderHistoryHeader(isRefreshing = uiState.isRefreshing)
        }
        items(uiState.orders, key = { it.id }) { order ->
            OrderHistoryCard(
                order = order,
                onViewDetailsClick = { onIntent(OrderHistoryUiIntent.OnViewDetailsClicked(order.id)) },
                onTrackPackageClick = { onIntent(OrderHistoryUiIntent.OnTrackPackageClicked(order.id)) },
            )
        }
    }
}

@Composable
private fun OrderHistoryHeader(
    isRefreshing: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.order_history_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
        )
        if (isRefreshing) {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun OrderHistoryEmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.order_history_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.order_history_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun OrderHistoryErrorContent(
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
                text = stringResource(R.string.order_history_retry),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
