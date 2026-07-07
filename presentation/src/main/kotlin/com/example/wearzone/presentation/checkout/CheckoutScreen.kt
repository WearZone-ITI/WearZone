package com.example.wearzone.presentation.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.checkout.components.CheckoutOrderSummaryCard
import com.example.wearzone.presentation.checkout.components.DeliveryAddressCard
import com.example.wearzone.presentation.checkout.components.PaymentMethodSelector
import com.example.wearzone.presentation.checkout.components.PromoCodeCard
import com.example.wearzone.presentation.common.SignInRequiredDialog
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.coroutines.launch
import com.paymob.paymob_sdk.ui.PaymobSdkListener

@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrderHistory: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToAddressList: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    refreshAfterAddressChange: Boolean = false,
    onRefreshAfterAddressChangeConsumed: () -> Unit = {},
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshAfterAddressChange) {
        if (refreshAfterAddressChange) {
            viewModel.handleIntent(CheckoutUiIntent.OnRefreshAddresses)
            onRefreshAfterAddressChangeConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                CheckoutUiEffect.NavigateBack -> onNavigateBack()
                CheckoutUiEffect.NavigateToOrderHistory -> onNavigateToOrderHistory()
                CheckoutUiEffect.NavigateToAddressList -> onNavigateToAddressList()
                CheckoutUiEffect.NavigateToAddAddress -> onNavigateToAddAddress()
                is CheckoutUiEffect.NavigateToPaymobSdk -> {
                    launchPaymobSdk(
                        context = context ,
                        clientSecret = effect.clientSecret,
                        paymobSdkListener = object : PaymobSdkListener {

                            override fun onSuccess(payResponse: HashMap<String, String?>) {
                                val transactionId = payResponse["id"] ?: payResponse["transaction_id"] ?: payResponse["txn_id"]
                                viewModel.handleIntent(
                                    CheckoutUiIntent.OnPaymobPaymentResult(
                                        isSuccess = true,
                                        transactionId = transactionId
                                    )
                                )
                            }

                            override fun onFailure(msg: String?) {
                                viewModel.handleIntent(
                                    CheckoutUiIntent.OnPaymobPaymentResult(
                                        isSuccess = false,
                                        transactionId = null,
                                        errorMessage = msg
                                    )
                                )
                            }

                            override fun onPending() {
                                viewModel.handleIntent(
                                    CheckoutUiIntent.OnPaymobPaymentResult(
                                        isSuccess = false,
                                        transactionId = null,
                                        errorMessage = "Payment Pending"
                                    )
                                )
                            }

                        }
                    )
                }

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
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToRegister = onNavigateToRegister,
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
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
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
                CheckoutUiState.SignInRequired -> CheckoutSignInRequiredContent(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    onContinueBrowsing = { onIntent(CheckoutUiIntent.OnBackClicked) },
                )
                is CheckoutUiState.Error -> CheckoutErrorContent(
                    messageRes = uiState.messageRes,
                    onRetry = { onIntent(CheckoutUiIntent.OnRetry) },
                )
                is CheckoutUiState.Content -> {
                    CheckoutLoadedContent(
                        state = uiState,
                        onIntent = onIntent,
                    )

                    if (uiState.isProcessingPayment || uiState.isPlacingOrder) {
                        PaymentLoadingOverlay(
                            isPlacingOrder = uiState.isPlacingOrder
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentLoadingOverlay(isPlacingOrder: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(260.dp)
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = AppTheme.colors.selected,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(
                        if (isPlacingOrder) R.string.checkout_placing_order
                        else R.string.checkout_securing_payment
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.checkout_do_not_close),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun CheckoutSignInRequiredContent(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueBrowsing: () -> Unit,
) {
    SignInRequiredDialog(
        onSignInRegister = onNavigateToLogin,
        onContinueBrowsing = onContinueBrowsing,
    )
}

@Composable
private fun CheckoutLoadedContent(
    state: CheckoutUiState.Content,
    onIntent: (CheckoutUiIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DeliveryAddressCard(
                address = state.deliveryAddress,
                isLoading = state.isLoadingAddress,
                onChangeClicked = { onIntent(CheckoutUiIntent.OnChangeAddressClicked) },
                onAddClicked = { onIntent(CheckoutUiIntent.OnAddAddressClicked) },
            )
        }
        item {
            PaymentMethodSelector(
                selectedMethod = state.paymentMethod,
                onMethodSelected = { onIntent(CheckoutUiIntent.OnPaymentMethodSelected(it)) }
            )
        }

        item {
            PromoCodeCard(
                promoCodeText = state.promoCodeText,
                appliedDiscountCode = state.appliedDiscountCode,
                discountErrorRes = state.discountErrorRes,
                isApplyingDiscount = state.isApplyingDiscount,
                onPromoCodeChanged = { onIntent(CheckoutUiIntent.OnPromoCodeChanged(it)) },
                onApplyClicked = { onIntent(CheckoutUiIntent.OnApplyDiscountClicked) },
                onRemoveClicked = { onIntent(CheckoutUiIntent.OnRemoveDiscountClicked) },
            )
        }
        item {
            CheckoutOrderSummaryCard(
                items = state.items,
                subtotal = state.subtotal,
                formattedDiscount = state.formattedDiscount,
                total = state.total,
            )
        }
        item { Spacer(modifier = Modifier.height(120.dp)) }
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
        if (state.formattedDiscount != null) {
            SummaryRow(
                labelRes = R.string.checkout_discount,
                value = "-${state.formattedDiscount}",
            )
        }
        SummaryRow(labelRes = R.string.cart_total, value = state.total)
        Button(
            onClick = onSubmit,
            enabled = !state.isPlacingOrder &&
                !state.isApplyingDiscount &&
                !state.isLoadingAddress &&
                !state.isProcessingPayment &&
                state.deliveryAddress != null ,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.selected,
                contentColor = AppTheme.colors.onAccent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            if (state.isPlacingOrder || state.isProcessingPayment) {
                CircularProgressIndicator(
                    color = AppTheme.colors.onAccent,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(R.string.checkout_place_order_with_total, state.total),
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
        modifier = Modifier.fillMaxWidth(0.95f),
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
