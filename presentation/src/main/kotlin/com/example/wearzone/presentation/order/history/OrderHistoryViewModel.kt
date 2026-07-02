package com.example.wearzone.presentation.order.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.account.model.OrderHistory
import com.example.wearzone.domain.account.model.OrderHistoryCustomerNotFoundException
import com.example.wearzone.domain.account.model.OrderHistoryNetworkException
import com.example.wearzone.domain.account.model.OrderHistoryPermissionException
import com.example.wearzone.domain.account.model.OrderHistoryResponseParseException
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.usecase.OrderHistoryUseCases
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Currency
import java.util.Locale
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderHistoryUseCases: OrderHistoryUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<OrderHistoryUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<OrderHistoryUiEffect> = _uiEffect.receiveAsFlow()

    private var customerId: Long? = null

    init {
        loadOrders()
    }

    fun handleIntent(intent: OrderHistoryUiIntent) {
        when (intent) {
            OrderHistoryUiIntent.OnBackClicked -> sendEffect(OrderHistoryUiEffect.NavigateBack)
            OrderHistoryUiIntent.OnRetry -> loadOrders()
            OrderHistoryUiIntent.OnRefresh -> loadOrders(isRefresh = true)
            is OrderHistoryUiIntent.OnViewDetailsClicked -> sendEffect(
                OrderHistoryUiEffect.ShowMessage(R.string.order_history_details_unavailable),
            )
            is OrderHistoryUiIntent.OnTrackPackageClicked -> sendEffect(
                OrderHistoryUiEffect.ShowMessage(R.string.order_history_tracking_available),
            )
        }
    }

    private fun loadOrders(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val previousContent = _uiState.value as? OrderHistoryUiState.Content
            setLoadingState(isRefresh, previousContent)
            val resolvedCustomerId = resolveCustomerId(previousContent, isRefresh) ?: return@launch

            orderHistoryUseCases.getOrderHistory(resolvedCustomerId)
                .onSuccess { orders ->
                    _uiState.value = if (orders.isEmpty()) {
                        OrderHistoryUiState.Empty
                    } else {
                        OrderHistoryUiState.Content(
                            orders = orders.map { it.toUiModel() }.toImmutableList(),
                        )
                    }
                }
                .onFailure { error ->
                    handleLoadFailure(error, previousContent, isRefresh)
                }
        }
    }

    private fun setLoadingState(
        isRefresh: Boolean,
        previousContent: OrderHistoryUiState.Content?,
    ) {
        _uiState.value = if (isRefresh && previousContent != null) {
            previousContent.copy(isRefreshing = true)
        } else {
            OrderHistoryUiState.Loading
        }
    }

    private suspend fun resolveCustomerId(
        previousContent: OrderHistoryUiState.Content?,
        isRefresh: Boolean,
    ): Long? {
        customerId?.let { return it }
        return orderHistoryUseCases.getCurrentCustomerId()
            .onSuccess { customerId = it }
            .onFailure { handleLoadFailure(it, previousContent, isRefresh) }
            .getOrNull()
    }

    private fun handleLoadFailure(
        error: Throwable,
        previousContent: OrderHistoryUiState.Content?,
        isRefresh: Boolean,
    ) {
        val messageRes = error.toMessageRes()
        if (isRefresh && previousContent != null) {
            _uiState.value = previousContent.copy(isRefreshing = false)
            sendEffect(OrderHistoryUiEffect.ShowMessage(messageRes))
        } else {
            _uiState.value = OrderHistoryUiState.Error(messageRes)
        }
    }

    private fun sendEffect(effect: OrderHistoryUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun OrderHistory.toUiModel(): OrderHistoryUiModel =
        OrderHistoryUiModel(
            id = id,
            displayName = name?.takeIf { it.isNotBlank() }
                ?: orderNumber?.let { "#$it" }
                ?: "",
            placedDate = createdAt.toPlacedDate(),
            formattedTotalPrice = totalPrice.toCurrencyText(currencyCode),
            statuses = buildStatuses().toImmutableList(),
            thumbnails = lineItems
                .take(3)
                .map { lineItem ->
                    OrderThumbnailUiModel(
                        id = lineItem.id,
                        imageUrl = lineItem.imageUrl?.takeIf { it.isNotBlank() },
                    )
                }
                .toImmutableList(),
            canTrack = !trackingUrl.isNullOrBlank() || !trackingNumber.isNullOrBlank(),
        )

    private fun OrderHistory.buildStatuses(): List<OrderStatusUiModel> {
        val statuses = mutableListOf<OrderStatusUiModel>()
        financialStatus?.toStatusUiModel()?.let(statuses::add)
        fulfillmentStatus?.toStatusUiModel()?.let(statuses::add)
        when (orderStatus) {
            OrderStatus.Cancelled -> statuses.add(
                OrderStatusUiModel(
                    labelRes = R.string.order_history_status_cancelled,
                    tone = OrderStatusTone.Error,
                )
            )
            OrderStatus.Closed -> statuses.add(
                OrderStatusUiModel(
                    labelRes = R.string.order_history_status_closed,
                    tone = OrderStatusTone.Neutral,
                )
            )
            OrderStatus.Open -> if (statuses.isEmpty()) {
                statuses.add(
                    OrderStatusUiModel(
                        labelRes = R.string.order_history_status_open,
                        tone = OrderStatusTone.Success,
                    )
                )
            }
        }
        return statuses
    }

    private fun String.toStatusUiModel(): OrderStatusUiModel? {
        val normalized = trim()
        if (normalized.isBlank()) return null
        val label = normalized.split('_', '-', ' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
            }
        val tone = when {
            normalized.contains("paid", ignoreCase = true) ||
                normalized.contains("fulfilled", ignoreCase = true) -> OrderStatusTone.Success
            normalized.contains("pending", ignoreCase = true) ||
                normalized.contains("partial", ignoreCase = true) ||
                normalized.contains("unfulfilled", ignoreCase = true) -> OrderStatusTone.Warning
            normalized.contains("cancel", ignoreCase = true) ||
                normalized.contains("refund", ignoreCase = true) ||
                normalized.contains("void", ignoreCase = true) -> OrderStatusTone.Error
            else -> OrderStatusTone.Neutral
        }
        return OrderStatusUiModel(label = label, tone = tone)
    }

    private fun String?.toPlacedDate(): String {
        if (isNullOrBlank()) return ""
        return try {
            OffsetDateTime.parse(this).format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()))
        } catch (_: DateTimeParseException) {
            this
        }
    }

    private fun Double.toCurrencyText(currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        if (currencyCode.isNotBlank()) {
            runCatching { format.currency = Currency.getInstance(currencyCode) }
        }
        return format.format(this)
    }

    private fun Throwable.toMessageRes(): Int =
        when (this) {
            is ShopifyCustomerIdUnavailableException -> R.string.order_history_error_customer_id_unavailable
            is OrderHistoryPermissionException -> R.string.order_history_error_permission
            is OrderHistoryCustomerNotFoundException -> R.string.order_history_error_customer_not_found
            is OrderHistoryResponseParseException -> R.string.order_history_error_response_parse
            is OrderHistoryNetworkException -> R.string.order_history_error_network
            else -> R.string.order_history_error_generic
        }
}
