package com.example.wearzone.presentation.order.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.order.history.OrderStatusTone
import kotlinx.coroutines.launch

@Composable
fun OrderDetailsScreen(
    orderId: Long,
    onNavigateBack: () -> Unit,
    onContinueShopping: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: OrderDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.handleIntent(OrderDetailsUiIntent.LoadOrder(orderId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                OrderDetailsUiEffect.NavigateBack -> onNavigateBack()
                OrderDetailsUiEffect.NavigateToShopping -> onContinueShopping()
                OrderDetailsUiEffect.ShowCancelOrderDialog -> showCancelDialog = true
                is OrderDetailsUiEffect.ShowMessage -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    OrderDetailsScaffold(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::handleIntent,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToRegister = onNavigateToRegister,
        onContinueBrowsing = onContinueShopping,
    )

    if (showCancelDialog) {
        CancelOrderDialog(
            onConfirm = {
                showCancelDialog = false
                viewModel.handleIntent(OrderDetailsUiIntent.OnCancelConfirmed)
            },
            onDismiss = { showCancelDialog = false },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OrderDetailsScaffold(
    uiState: OrderDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (OrderDetailsUiIntent) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueBrowsing: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = stringResource(R.string.order_details_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(OrderDetailsUiIntent.OnBackClicked) }) {
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
                OrderDetailsUiState.Loading -> CircularProgressIndicator(
                    color = AppTheme.colors.selected,
                    modifier = Modifier.align(Alignment.Center),
                )
                OrderDetailsUiState.SignInRequired -> OrderDetailsSignInRequiredContent(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    onContinueBrowsing = onContinueBrowsing,
                )
                is OrderDetailsUiState.Error -> OrderDetailsErrorContent(
                    messageRes = uiState.messageRes,
                    onRetry = { onIntent(OrderDetailsUiIntent.OnRetry) },
                )
                is OrderDetailsUiState.Content -> OrderDetailsLoadedContent(
                    state = uiState,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsSignInRequiredContent(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueBrowsing: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.sign_in_required_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.sign_in_required_message),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onNavigateToLogin,
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
                text = stringResource(R.string.sign_in),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onNavigateToRegister,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.create_account),
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onContinueBrowsing,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.continue_browsing),
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun OrderDetailsLoadedContent(
    state: OrderDetailsUiState.Content,
    onIntent: (OrderDetailsUiIntent) -> Unit,
) {
    val order = state.order
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            OrderDetailsHeader(order = order, isRefreshing = state.isRefreshing)
        }
        item {
            TimelineCard(timeline = order.timeline)
        }
        item {
            SectionTitle(titleRes = R.string.order_details_items_title)
        }
        if (order.items.isEmpty()) {
            item { EmptyItemsCard() }
        } else {
            items(order.items, key = { it.id }) { item ->
                OrderItemRow(item = item)
            }
        }
        order.shippingAddress?.let { address ->
            item {
                AddressCard(address = address)
            }
        }
        if (order.paymentMethods.isNotEmpty()) {
            item {
                PaymentCard(paymentMethods = order.paymentMethods.joinToString(", "))
            }
        }
        item {
            SummaryCard(order = order)
        }
        if (order.canCancel) {
            item {
                Button(
                    onClick = { onIntent(OrderDetailsUiIntent.OnCancelOrderClicked) },
                    enabled = !state.isCanceling,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.error,
                        contentColor = AppTheme.colors.onAccent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    if (state.isCanceling) {
                        CircularProgressIndicator(
                            color = AppTheme.colors.onAccent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.order_details_cancel_order),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { onIntent(OrderDetailsUiIntent.OnContinueShoppingClicked) },
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, AppTheme.colors.textPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    text = stringResource(R.string.order_details_continue_shopping),
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsHeader(
    order: OrderDetailsUiModel,
    isRefreshing: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.order_details_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.order_details_order_number, order.displayName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StatusChip(status = order.status)
        }
        if (isRefreshing) {
            CircularProgressIndicator(
                color = AppTheme.colors.selected,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(22.dp),
            )
        }
    }
}

@Composable
private fun StatusChip(status: OrderDetailsStatusUiModel) {
    val backgroundColor = when (status.tone) {
        OrderStatusTone.Success -> AppTheme.colors.success.copy(alpha = 0.14f)
        OrderStatusTone.Warning -> AppTheme.colors.warning.copy(alpha = 0.16f)
        OrderStatusTone.Error -> AppTheme.colors.error.copy(alpha = 0.14f)
        OrderStatusTone.Neutral -> AppTheme.colors.surfaceVariant
    }
    val contentColor = when (status.tone) {
        OrderStatusTone.Success -> AppTheme.colors.success
        OrderStatusTone.Warning -> AppTheme.colors.warning
        OrderStatusTone.Error -> AppTheme.colors.error
        OrderStatusTone.Neutral -> AppTheme.colors.textSecondary
    }
    val label = status.labelRes?.let { stringResource(it) } ?: status.label.orEmpty()
    Surface(color = backgroundColor, contentColor = contentColor, shape = RoundedCornerShape(50)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TimelineCard(timeline: List<OrderTimelineUiModel>) {
    DetailsCard {
        Text(
            text = stringResource(R.string.order_details_tracking_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            timeline.forEach { item ->
                TimelineRow(item = item)
            }
        }
    }
}

@Composable
private fun TimelineRow(item: OrderTimelineUiModel) {
    val description = if (item.description.isBlank()) {
        stringResource(R.string.order_details_timeline_unavailable)
    } else {
        item.description
    }
    Row(verticalAlignment = Alignment.Top) {
        val icon = when (item.tone) {
            OrderStatusTone.Success -> Icons.Outlined.Check
            OrderStatusTone.Warning -> Icons.Outlined.Schedule
            OrderStatusTone.Error -> Icons.Outlined.WarningAmber
            OrderStatusTone.Neutral -> Icons.Outlined.LocalShipping
        }
        Surface(
            shape = CircleShape,
            color = if (item.isActive) AppTheme.colors.selected else AppTheme.colors.surfaceVariant,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (item.isActive) AppTheme.colors.onAccent else AppTheme.colors.textSecondary,
                modifier = Modifier.padding(5.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = stringResource(item.labelRes),
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.isActive) AppTheme.colors.textPrimary else AppTheme.colors.textSecondary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun SectionTitle(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleLarge,
        color = AppTheme.colors.textPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun OrderItemRow(item: OrderDetailsItemUiModel) {
    DetailsCard(contentPadding = PaddingValues(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 88.dp, height = 108.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (item.imageUrl.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.order_details_image_placeholder),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = stringResource(
                            R.string.order_details_product_image_content_description,
                            item.title,
                        ),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.variantInfo.isNotBlank()) {
                    Text(
                        text = item.variantInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.order_details_quantity, item.quantity),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyItemsCard() {
    DetailsCard {
        Text(
            text = stringResource(R.string.order_details_empty_items),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun AddressCard(address: OrderDetailsAddressUiModel) {
    DetailsCard {
        IconTitleRow(
            icon = Icons.Outlined.LocationOn,
            titleRes = R.string.order_details_shipping_address,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = address.recipientName,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
        )
        Text(
            text = address.addressLines,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.padding(top = 6.dp),
        )
        address.phone?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun PaymentCard(paymentMethods: String) {
    DetailsCard {
        IconTitleRow(
            icon = Icons.Outlined.CreditCard,
            titleRes = R.string.order_details_payment_method,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = paymentMethods,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun SummaryCard(order: OrderDetailsUiModel) {
    DetailsCard {
        Text(
            text = stringResource(R.string.order_details_summary_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(18.dp))
        SummaryRow(R.string.order_details_subtotal, order.subtotal)
        SummaryRow(R.string.order_details_shipping, order.shipping)
        SummaryRow(R.string.order_details_tax, order.tax)
        HorizontalDivider(
            color = AppTheme.colors.divider,
            modifier = Modifier.padding(vertical = 14.dp),
        )
        SummaryRow(
            labelRes = R.string.cart_total,
            value = order.total,
            isTotal = true,
        )
    }
}

@Composable
private fun SummaryRow(
    labelRes: Int,
    value: String,
    isTotal: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(labelRes),
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun IconTitleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titleRes: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colors.textPrimary,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DetailsCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(22.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppTheme.colors.card,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}

@Composable
private fun OrderDetailsErrorContent(
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
        Icon(
            imageVector = Icons.Outlined.Inventory2,
            contentDescription = null,
            tint = AppTheme.colors.error,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 18.dp),
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

@Composable
private fun CancelOrderDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.order_details_cancel_dialog_title)) },
        text = { Text(text = stringResource(R.string.order_details_cancel_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.order_details_cancel_dialog_confirm),
                    color = AppTheme.colors.error,
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
