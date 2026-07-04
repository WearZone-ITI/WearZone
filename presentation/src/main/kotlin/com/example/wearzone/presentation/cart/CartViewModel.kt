package com.example.wearzone.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.ClearCartUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.cart.usecase.RemoveFromCartUseCase
import com.example.wearzone.domain.cart.usecase.UpdateCartQuantityUseCase
import com.example.wearzone.domain.common.DataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val observeCartUseCase: ObserveCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
) : ViewModel() {

    private val cartItems =
        observeCartUseCase()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val uiState =
        cartItems
            .combineWithCartState()
            .catch {
                emit(
                    CartUiState.Error(
                        it.localizedMessage ?: GENERIC_ERROR_MESSAGE
                    )
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                CartUiState.Loading
            )

    private val _uiEffect = Channel<CartUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CartUiEffect> = _uiEffect.receiveAsFlow()

    fun handleIntent(intent: CartUiIntent) {
        when (intent) {
            CartUiIntent.OnRetry -> Unit
            CartUiIntent.OnCheckoutClicked -> checkout()
            CartUiIntent.OnClearCartClicked -> requestClearCart()
            CartUiIntent.OnClearCartConfirmed -> clearCart()
            is CartUiIntent.OnIncreaseQuantity -> changeQuantity(intent.variantId, QUANTITY_STEP)
            is CartUiIntent.OnDecreaseQuantity -> changeQuantity(intent.variantId, -QUANTITY_STEP)
            is CartUiIntent.OnRemoveItemClicked -> requestRemoveItem(intent.variantId)
            is CartUiIntent.OnRemoveItemConfirmed -> removeItem(intent.variantId)
        }
    }

    private fun buildCartState(items: List<CartItem>): CartUiState {
        if (items.isEmpty()) return CartUiState.Empty

        val subtotal = items.sumOf { it.price * it.quantity }
        val currencyCode = items.firstOrNull()?.currencyCode.orEmpty()
        return CartUiState.Content(
            items = items.map { it.toUiModel() }.toImmutableList(),
            itemCount = items.sumOf { it.quantity },
            subtotal = formatMoney(subtotal, currencyCode),
            total = formatMoney(subtotal, currencyCode),
        )
    }

    private fun changeQuantity(variantId: String, delta: Int) {
        val item = currentItems().firstOrNull { it.variantId == variantId } ?: return
        val newQuantity = (item.quantity + delta).coerceIn(MIN_QUANTITY, item.maxQuantity.coerceAtLeast(MIN_QUANTITY))
        if (newQuantity == item.quantity) return

        viewModelScope.launch {
            when (updateCartQuantityUseCase(variantId, newQuantity)) {
                is DataResult.Success -> Unit
                is DataResult.Error -> _uiEffect.send(CartUiEffect.ShowSnackbar(GENERIC_ERROR_MESSAGE))
            }
        }
    }

    private fun requestRemoveItem(variantId: String) {
        viewModelScope.launch {
            _uiEffect.send(CartUiEffect.ShowRemoveConfirmation(variantId))
        }
    }

    private fun removeItem(variantId: String) {
        viewModelScope.launch {
            when (removeFromCartUseCase(variantId)) {
                is DataResult.Success -> Unit
                is DataResult.Error -> _uiEffect.send(CartUiEffect.ShowSnackbar(GENERIC_ERROR_MESSAGE))
            }
        }
    }

    private fun requestClearCart() {
        viewModelScope.launch {
            _uiEffect.send(CartUiEffect.ShowClearCartConfirmation)
        }
    }

    private fun clearCart() {
        viewModelScope.launch {
            when (clearCartUseCase()) {
                is DataResult.Success -> Unit
                is DataResult.Error -> _uiEffect.send(CartUiEffect.ShowSnackbar(GENERIC_ERROR_MESSAGE))
            }
        }
    }

    private fun checkout() {
        viewModelScope.launch {
            when (getAuthAccessStateUseCase()) {
                is AuthAccessState.AuthenticatedCustomer -> _uiEffect.send(CartUiEffect.NavigateToCheckout)
                AuthAccessState.AuthenticatedMissingCustomerId,
                AuthAccessState.Guest,
                -> _uiEffect.send(CartUiEffect.ShowSignInRequired)
            }
        }
    }

    private fun Flow<List<CartItem>>.combineWithCartState(): Flow<CartUiState> =
        map { items -> buildCartState(items) }

    private fun currentItems(): List<CartItemUiModel> {
        return (uiState.value as? CartUiState.Content)?.items.orEmpty()
    }

    private fun CartItem.toUiModel(): CartItemUiModel = CartItemUiModel(
        variantId = variantId,
        productId = productId,
        title = title,
        vendor = vendor,
        price = formatMoney(price, currencyCode),
        quantity = quantity,
        maxQuantity = maxQuantity,
        imageUrl = imageUrl,
        size = size,
        isLowStock = maxQuantity - quantity <= LOW_STOCK_THRESHOLD,
    )

    private fun formatMoney(amount: Double, currencyCode: String): String {
        val suffix = currencyCode.ifBlank { DEFAULT_CURRENCY_CODE }
        return String.format(Locale.US, "%.2f %s", amount, suffix)
    }

    private companion object {
        const val MIN_QUANTITY = 1
        const val QUANTITY_STEP = 1
        const val LOW_STOCK_THRESHOLD = 2
        const val DEFAULT_CURRENCY_CODE = "USD"
        const val GENERIC_ERROR_MESSAGE = "Unable to update cart. Please try again."
    }
}
