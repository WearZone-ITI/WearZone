package com.example.wearzone.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.checkout.model.CheckoutCartClearException
import com.example.wearzone.domain.checkout.model.CheckoutOrderCreationException
import com.example.wearzone.domain.checkout.model.EmptyCartCheckoutException
import com.example.wearzone.domain.checkout.model.InvalidCheckoutLineItemException
import com.example.wearzone.domain.checkout.usecase.PlaceOrderUseCase
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val observeCartUseCase: ObserveCartUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
) : ViewModel() {

    private val isPlacingOrder = MutableStateFlow(false)

    private val cartItems = observeCartUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    val uiState = combine(cartItems, isPlacingOrder) { items, placingOrder ->
        buildState(items, placingOrder)
    }
        .catch { emit(CheckoutUiState.Error(R.string.checkout_error_generic)) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CheckoutUiState.Loading,
        )

    private val _uiEffect = Channel<CheckoutUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CheckoutUiEffect> = _uiEffect.receiveAsFlow()

    fun handleIntent(intent: CheckoutUiIntent) {
        when (intent) {
            CheckoutUiIntent.OnBackClicked -> sendEffect(CheckoutUiEffect.NavigateBack)
            CheckoutUiIntent.OnRetry -> Unit
            CheckoutUiIntent.OnSubmitOrderClicked -> requestOrderConfirmation()
            CheckoutUiIntent.OnSubmitOrderConfirmed -> submitOrder()
        }
    }

    private fun buildState(items: List<CartItem>, placingOrder: Boolean): CheckoutUiState {
        if (items.isEmpty()) return CheckoutUiState.Empty

        val subtotal = items.sumOf { it.price * it.quantity }
        val currencyCode = items.firstOrNull()?.currencyCode.orEmpty()
        return CheckoutUiState.Content(
            items = items.map { it.toUiModel() }.toImmutableList(),
            itemCount = items.sumOf { it.quantity },
            subtotal = formatMoney(subtotal, currencyCode),
            total = formatMoney(subtotal, currencyCode),
            isPlacingOrder = placingOrder,
        )
    }

    private fun requestOrderConfirmation() {
        viewModelScope.launch {
            if (uiState.value !is CheckoutUiState.Content) {
                _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_error_empty_cart))
                return@launch
            }
            _uiEffect.send(CheckoutUiEffect.ShowConfirmOrderDialog)
        }
    }

    private fun submitOrder() {
        viewModelScope.launch {
            if (isPlacingOrder.value) return@launch

            isPlacingOrder.value = true
            placeOrderUseCase()
                .onSuccess {
                    isPlacingOrder.value = false
                    _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_order_success))
                }
                .onFailure { error ->
                    isPlacingOrder.value = false
                    _uiEffect.send(CheckoutUiEffect.ShowMessage(error.toMessageRes()))
                }
        }
    }

    private fun sendEffect(effect: CheckoutUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun CartItem.toUiModel(): CheckoutCartItemUiModel =
        CheckoutCartItemUiModel(
            variantId = variantId,
            title = title,
            quantity = quantity,
            formattedPrice = formatMoney(price * quantity, currencyCode),
        )

    private fun formatMoney(amount: Double, currencyCode: String): String {
        val suffix = currencyCode.ifBlank { DEFAULT_CURRENCY_CODE }
        return String.format(Locale.US, "%.2f %s", amount, suffix)
    }

    private fun Throwable.toMessageRes(): Int =
        when (this) {
            is EmptyCartCheckoutException -> R.string.checkout_error_empty_cart
            is InvalidCheckoutLineItemException -> R.string.checkout_error_invalid_cart
            is ShopifyCustomerIdUnavailableException -> R.string.checkout_error_customer_id_unavailable
            is CheckoutCartClearException -> R.string.checkout_error_clear_cart
            is CheckoutOrderCreationException -> R.string.checkout_error_order_creation
            else -> R.string.checkout_error_generic
        }

    private companion object {
        const val DEFAULT_CURRENCY_CODE = "USD"
    }
}
