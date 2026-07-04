package com.example.wearzone.presentation.order.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.account.model.OrderCancelNotAllowedException
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.model.OrderDetailsAddress
import com.example.wearzone.domain.account.model.OrderDetailsLineItem
import com.example.wearzone.domain.account.model.OrderDetailsNotFoundException
import com.example.wearzone.domain.account.model.OrderDetailsOwnershipMismatchException
import com.example.wearzone.domain.account.model.OrderHistoryCustomerNotFoundException
import com.example.wearzone.domain.account.model.OrderHistoryNetworkException
import com.example.wearzone.domain.account.model.OrderHistoryPermissionException
import com.example.wearzone.domain.account.model.OrderHistoryResponseParseException
import com.example.wearzone.domain.account.model.OrderStatus
import com.example.wearzone.domain.account.usecase.OrderHistoryUseCases
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import com.example.wearzone.presentation.order.history.OrderStatusTone
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val orderHistoryUseCases: OrderHistoryUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderDetailsUiState>(OrderDetailsUiState.Loading)
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<OrderDetailsUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<OrderDetailsUiEffect> = _uiEffect.receiveAsFlow()

    private var orderId: Long? = null
    private var customerId: Long? = null

    fun handleIntent(intent: OrderDetailsUiIntent) {
        when (intent) {
            is OrderDetailsUiIntent.LoadOrder -> loadOrder(intent.orderId)
            OrderDetailsUiIntent.OnBackClicked -> sendEffect(OrderDetailsUiEffect.NavigateBack)
            OrderDetailsUiIntent.OnRetry -> orderId?.let(::loadOrder)
            OrderDetailsUiIntent.OnCancelOrderClicked -> requestCancel()
            OrderDetailsUiIntent.OnCancelConfirmed -> cancelOrder()
            OrderDetailsUiIntent.OnContinueShoppingClicked -> sendEffect(OrderDetailsUiEffect.NavigateToShopping)
        }
    }

    private fun loadOrder(orderId: Long, isRefresh: Boolean = false) {
        this.orderId = orderId
        viewModelScope.launch {
            val previousContent = _uiState.value as? OrderDetailsUiState.Content
            _uiState.value = if (isRefresh && previousContent != null) {
                previousContent.copy(isRefreshing = true)
            } else {
                OrderDetailsUiState.Loading
            }

            val resolvedCustomerId = resolveCustomerId() ?: return@launch
            orderHistoryUseCases.getOrderDetails(orderId, resolvedCustomerId)
                .onSuccess { order ->
                    _uiState.value = OrderDetailsUiState.Content(order = order.toUiModel())
                }
                .onFailure { error ->
                    if (isRefresh && previousContent != null) {
                        _uiState.value = previousContent.copy(isRefreshing = false)
                        sendEffect(OrderDetailsUiEffect.ShowMessage(error.toMessageRes()))
                    } else {
                        _uiState.value = OrderDetailsUiState.Error(error.toMessageRes())
                    }
                }
        }
    }

    private suspend fun resolveCustomerId(): Long? {
        customerId?.let { return it }
        return orderHistoryUseCases.getCurrentCustomerId()
            .onSuccess { customerId = it }
            .onFailure {
                _uiState.value = OrderDetailsUiState.Error(it.toMessageRes())
            }
            .getOrNull()
    }

    private fun requestCancel() {
        val content = _uiState.value as? OrderDetailsUiState.Content ?: return
        if (!content.order.canCancel || content.isCanceling) {
            sendEffect(OrderDetailsUiEffect.ShowMessage(R.string.order_details_cancel_not_allowed))
            return
        }
        sendEffect(OrderDetailsUiEffect.ShowCancelOrderDialog)
    }

    private fun cancelOrder() {
        val currentOrderId = orderId ?: return
        val content = _uiState.value as? OrderDetailsUiState.Content ?: return
        if (!content.order.canCancel || content.isCanceling) return

        viewModelScope.launch {
            _uiState.value = content.copy(isCanceling = true)
            orderHistoryUseCases.cancelOrder(currentOrderId)
                .onSuccess {
                    sendEffect(OrderDetailsUiEffect.ShowMessage(R.string.order_details_cancel_success))
                    loadOrder(currentOrderId, isRefresh = true)
                }
                .onFailure { error ->
                    _uiState.value = content.copy(isCanceling = false)
                    sendEffect(OrderDetailsUiEffect.ShowMessage(error.toCancelMessageRes()))
                }
        }
    }

    private fun sendEffect(effect: OrderDetailsUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun OrderDetails.toUiModel(): OrderDetailsUiModel =
        OrderDetailsUiModel(
            id = id,
            displayName = name?.takeIf { it.isNotBlank() }
                ?: orderNumber?.let { "#$it" }
                ?: "",
            placedDate = createdAt.toDisplayDateTime(),
            status = toStatusUiModel(),
            timeline = buildTimeline().toImmutableList(),
            items = lineItems.map { it.toUiModel(currencyCode) }.toImmutableList(),
            shippingAddress = shippingAddress?.toUiModel(),
            paymentMethods = paymentMethods.toImmutableList(),
            subtotal = subtotalPrice.toCurrencyText(currencyCode),
            shipping = shippingPrice.toCurrencyText(currencyCode),
            tax = taxPrice.toCurrencyText(currencyCode),
            total = totalPrice.toCurrencyText(currencyCode),
            canCancel = canCancel,
        )

    private fun OrderDetails.toStatusUiModel(): OrderDetailsStatusUiModel {
        val label = when (orderStatus) {
            OrderStatus.Cancelled -> R.string.order_history_status_cancelled
            OrderStatus.Closed -> R.string.order_history_status_closed
            OrderStatus.Open -> null
        }
        return if (label != null) {
            OrderDetailsStatusUiModel(
                labelRes = label,
                tone = if (orderStatus == OrderStatus.Cancelled) OrderStatusTone.Error else OrderStatusTone.Neutral,
            )
        } else {
            val status = fulfillmentStatus?.takeIf { it.isNotBlank() }
                ?: financialStatus?.takeIf { it.isNotBlank() }
                ?: return OrderDetailsStatusUiModel(
                    labelRes = R.string.order_history_status_open,
                    tone = OrderStatusTone.Success,
                )
            OrderDetailsStatusUiModel(label = status.toTitleCase(), tone = status.toTone())
        }
    }

    private fun OrderDetails.buildTimeline(): List<OrderTimelineUiModel> {
        val timeline = mutableListOf<OrderTimelineUiModel>()
        timeline.add(
            OrderTimelineUiModel(
                labelRes = R.string.order_details_timeline_placed,
                description = createdAt.toDisplayDateTime(),
                tone = OrderStatusTone.Success,
                isActive = true,
            )
        )
        val processingDescription = financialStatus?.toTitleCase().orEmpty()
        timeline.add(
            OrderTimelineUiModel(
                labelRes = R.string.order_details_timeline_processing,
                description = processingDescription,
                tone = financialStatus.orEmpty().toTone(),
                isActive = true,
            )
        )
        if (orderStatus == OrderStatus.Cancelled) {
            timeline.add(
                OrderTimelineUiModel(
                    labelRes = R.string.order_details_timeline_cancelled,
                    description = cancelledAt.toDisplayDateTime(),
                    tone = OrderStatusTone.Error,
                    isActive = true,
                )
            )
        } else {
            timeline.add(
                OrderTimelineUiModel(
                    labelRes = R.string.order_details_timeline_shipped,
                    description = trackingNumber?.takeIf { it.isNotBlank() }
                        ?: trackingUrl?.takeIf { it.isNotBlank() }
                        ?: fulfillmentStatus?.toTitleCase().orEmpty(),
                    tone = fulfillmentStatus.orEmpty().toTone(),
                    isActive = !fulfillmentStatus.isNullOrBlank(),
                )
            )
        }
        return timeline
    }

    private fun OrderDetailsLineItem.toUiModel(currencyCode: String): OrderDetailsItemUiModel =
        OrderDetailsItemUiModel(
            id = id,
            title = title,
            variantInfo = variantTitle.orEmpty(),
            quantity = quantity,
            formattedPrice = (price * quantity).toCurrencyText(currencyCode),
            imageUrl = imageUrl,
        )

    private fun OrderDetailsAddress.toUiModel(): OrderDetailsAddressUiModel {
        val recipient = name?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "-" }
        val cityLine = listOfNotNull(city, province, zip)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        return OrderDetailsAddressUiModel(
            recipientName = recipient,
            addressLines = listOfNotNull(address1, address2, cityLine.ifBlank { null }, country)
                .filter { it.isNotBlank() }
                .joinToString("\n"),
            phone = phone?.takeIf { it.isNotBlank() },
        )
    }

    private fun String?.toDisplayDateTime(): String {
        if (isNullOrBlank()) return ""
        return try {
            OffsetDateTime.parse(this).format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a", Locale.getDefault()),
            )
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

    private fun String.toTitleCase(): String =
        split('_', '-', ' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                }
            }

    private fun String.toTone(): OrderStatusTone =
        when {
            contains("cancel", ignoreCase = true) ||
                contains("refund", ignoreCase = true) ||
                contains("void", ignoreCase = true) -> OrderStatusTone.Error
            contains("pending", ignoreCase = true) ||
                contains("partial", ignoreCase = true) ||
                contains("unfulfilled", ignoreCase = true) -> OrderStatusTone.Warning
            contains("paid", ignoreCase = true) ||
                contains("fulfilled", ignoreCase = true) ||
                contains("delivered", ignoreCase = true) -> OrderStatusTone.Success
            else -> OrderStatusTone.Neutral
        }

    private fun Throwable.toMessageRes(): Int =
        when (this) {
            is ShopifyCustomerIdUnavailableException -> R.string.order_history_error_customer_id_unavailable
            is OrderHistoryPermissionException -> R.string.order_history_error_permission
            is OrderHistoryCustomerNotFoundException -> R.string.order_history_error_customer_not_found
            is OrderDetailsNotFoundException -> R.string.order_details_error_not_found
            is OrderDetailsOwnershipMismatchException -> R.string.order_details_error_permission
            is OrderHistoryResponseParseException -> R.string.order_history_error_response_parse
            is OrderHistoryNetworkException -> R.string.order_history_error_network
            else -> R.string.order_details_error_generic
        }

    private fun Throwable.toCancelMessageRes(): Int =
        when (this) {
            is OrderCancelNotAllowedException -> R.string.order_details_cancel_not_allowed
            is OrderHistoryPermissionException -> R.string.order_history_error_permission
            is OrderHistoryNetworkException -> R.string.order_history_error_network
            else -> R.string.order_details_cancel_error
        }
}
