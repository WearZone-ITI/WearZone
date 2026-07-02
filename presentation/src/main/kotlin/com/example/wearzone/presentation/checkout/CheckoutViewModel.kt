package com.example.wearzone.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.checkout.model.CheckoutCartClearException
import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import com.example.wearzone.domain.checkout.model.CheckoutOrderCreationException
import com.example.wearzone.domain.checkout.model.CheckoutPaymentMethod
import com.example.wearzone.domain.checkout.model.DiscountLookupException
import com.example.wearzone.domain.checkout.model.EmptyCartCheckoutException
import com.example.wearzone.domain.checkout.model.EmptyDiscountCodeException
import com.example.wearzone.domain.checkout.model.InvalidCheckoutLineItemException
import com.example.wearzone.domain.checkout.model.InvalidDiscountCodeException
import com.example.wearzone.domain.checkout.model.MissingCheckoutAddressException
import com.example.wearzone.domain.checkout.usecase.ApplyDiscountCodeUseCase
import com.example.wearzone.domain.checkout.usecase.PlaceOrderUseCase
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import com.example.wearzone.domain.customer.address.usecase.CustomerAddressUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
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
    private val applyDiscountCodeUseCase: ApplyDiscountCodeUseCase,
    private val customerAddressUseCases: CustomerAddressUseCases,
) : ViewModel() {

    private val isPlacingOrder = MutableStateFlow(false)
    private val promoState = MutableStateFlow(PromoState())
    private val checkoutDetails = MutableStateFlow(CheckoutDetailsState())

    private val cartItems = observeCartUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    val uiState = combine(cartItems, isPlacingOrder, promoState, checkoutDetails) { items, placingOrder, promo, details ->
        buildState(
            items = items,
            placingOrder = placingOrder,
            promoState = promo,
            checkoutDetails = details,
        )
    }
        .catch { emit(CheckoutUiState.Error(R.string.checkout_error_generic)) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CheckoutUiState.Loading,
        )

    private val _uiEffect = Channel<CheckoutUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CheckoutUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadDeliveryAddress()
    }

    fun handleIntent(intent: CheckoutUiIntent) {
        when (intent) {
            CheckoutUiIntent.OnBackClicked -> sendEffect(CheckoutUiEffect.NavigateBack)
            CheckoutUiIntent.OnRetry -> retryLoad()
            CheckoutUiIntent.OnSubmitOrderClicked -> requestOrderConfirmation()
            CheckoutUiIntent.OnSubmitOrderConfirmed -> submitOrder()
            is CheckoutUiIntent.OnPromoCodeChanged -> onPromoCodeChanged(intent.text)
            CheckoutUiIntent.OnApplyDiscountClicked -> applyDiscount()
            CheckoutUiIntent.OnRemoveDiscountClicked -> removeDiscount()
            CheckoutUiIntent.OnChangeAddressClicked -> sendEffect(CheckoutUiEffect.NavigateToAddressList)
            CheckoutUiIntent.OnAddAddressClicked -> sendEffect(CheckoutUiEffect.NavigateToAddAddress)
            CheckoutUiIntent.OnRefreshAddresses -> loadDeliveryAddress()
        }
    }

    private fun buildState(
        items: List<CartItem>,
        placingOrder: Boolean,
        promoState: PromoState,
        checkoutDetails: CheckoutDetailsState,
    ): CheckoutUiState {
        if (items.isEmpty()) return CheckoutUiState.Empty

        val subtotalAmount = items.sumOf { it.price * it.quantity }
        val currencyCode = items.firstOrNull()?.currencyCode.orEmpty()
        val discountAmount = promoState.appliedDiscount?.calculatedAmount ?: 0.0
        val totalAmount = max(subtotalAmount - discountAmount, 0.0)

        return CheckoutUiState.Content(
            items = items.map { it.toUiModel() }.toImmutableList(),
            itemCount = items.sumOf { it.quantity },
            subtotal = formatMoney(subtotalAmount, currencyCode),
            formattedDiscount = promoState.appliedDiscount?.let {
                formatMoney(discountAmount, currencyCode)
            },
            total = formatMoney(totalAmount, currencyCode),
            promoCodeText = promoState.promoCodeText,
            appliedDiscountCode = promoState.appliedDiscount?.code,
            discountErrorRes = promoState.discountErrorRes,
            isApplyingDiscount = promoState.isApplyingDiscount,
            isPlacingOrder = placingOrder,
            deliveryAddress = checkoutDetails.deliveryAddress,
            isLoadingAddress = checkoutDetails.isLoadingAddress,
            paymentMethod = checkoutDetails.paymentMethod,
        )
    }

    private fun loadDeliveryAddress() {
        viewModelScope.launch {
            checkoutDetails.value = checkoutDetails.value.copy(isLoadingAddress = true)
            val customerId = customerAddressUseCases.getCurrentCustomerId().getOrElse {
                checkoutDetails.value = checkoutDetails.value.copy(isLoadingAddress = false)
                return@launch
            }
            customerAddressUseCases.getAddresses(customerId)
                .onSuccess { addresses ->
                    val selected = checkoutDetails.value.selectedAddressId?.let { selectedId ->
                        addresses.firstOrNull { it.id == selectedId }
                    } ?: addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
                    checkoutDetails.value = checkoutDetails.value.copy(
                        isLoadingAddress = false,
                        selectedAddressId = selected?.id,
                        deliveryAddress = selected?.toDeliveryUiModel(),
                    )
                }
                .onFailure {
                    checkoutDetails.value = checkoutDetails.value.copy(isLoadingAddress = false)
                }
        }
    }

    private fun onPromoCodeChanged(text: String) {
        promoState.value = promoState.value.copy(
            promoCodeText = text,
            discountErrorRes = null,
        )
    }

    private fun applyDiscount() {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) {
                _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_error_empty_cart))
                return@launch
            }

            promoState.value = promoState.value.copy(isApplyingDiscount = true)
            val subtotal = items.sumOf { it.price * it.quantity }
            applyDiscountCodeUseCase(promoState.value.promoCodeText, subtotal)
                .onSuccess { discount ->
                    promoState.value = promoState.value.copy(
                        appliedDiscount = discount,
                        discountErrorRes = null,
                        promoCodeText = discount.code,
                        isApplyingDiscount = false,
                    )
                    _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_discount_applied))
                }
                .onFailure { error ->
                    promoState.value = promoState.value.copy(
                        isApplyingDiscount = false,
                        discountErrorRes = error.toDiscountMessageRes(),
                    )
                }
        }
    }

    private fun removeDiscount() {
        promoState.value = PromoState()
    }

    private fun requestOrderConfirmation() {
        viewModelScope.launch {
            if (uiState.value !is CheckoutUiState.Content) {
                _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_error_empty_cart))
                return@launch
            }
            if (checkoutDetails.value.deliveryAddress == null) {
                _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_error_no_address))
                return@launch
            }
            _uiEffect.send(CheckoutUiEffect.ShowConfirmOrderDialog)
        }
    }

    private fun submitOrder() {
        viewModelScope.launch {
            if (isPlacingOrder.value) return@launch

            isPlacingOrder.value = true
            placeOrderUseCase(
                discount = promoState.value.appliedDiscount,
                selectedAddressId = checkoutDetails.value.selectedAddressId,
                paymentMethod = CheckoutPaymentMethod.CashOnDelivery,
            )
                .onSuccess {
                    isPlacingOrder.value = false
                    promoState.value = PromoState()
                    _uiEffect.send(CheckoutUiEffect.NavigateToOrderHistory)
                }
                .onFailure { error ->
                    isPlacingOrder.value = false
                    _uiEffect.send(CheckoutUiEffect.ShowMessage(error.toMessageRes()))
                }
        }
    }

    private fun retryLoad() {
        viewModelScope.launch {
            promoState.value = promoState.value.copy(discountErrorRes = null)
            loadDeliveryAddress()
        }
    }

    private fun sendEffect(effect: CheckoutUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun CustomerAddress.toDeliveryUiModel(): CheckoutDeliveryAddressUiModel {
        val recipient = name?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "-" }
        val cityParts = listOfNotNull(city, province, zip).filter { it.isNotBlank() }
        return CheckoutDeliveryAddressUiModel(
            id = id,
            recipientName = recipient,
            addressLines = listOfNotNull(address1, address2, cityParts.joinToString(", ").ifBlank { null })
                .filter { it.isNotBlank() }
                .joinToString("\n"),
            countryLine = countryName ?: country.orEmpty(),
        )
    }

    private fun CartItem.toUiModel(): CheckoutCartItemUiModel =
        CheckoutCartItemUiModel(
            variantId = variantId,
            title = title,
            vendor = vendor,
            quantity = quantity,
            formattedPrice = formatMoney(price * quantity, currencyCode),
            imageUrl = imageUrl,
        )

    private fun formatMoney(amount: Double, currencyCode: String): String {
        val suffix = currencyCode.ifBlank { DEFAULT_CURRENCY_CODE }
        return String.format(Locale.US, "%.2f %s", amount, suffix)
    }

    private fun Throwable.toDiscountMessageRes(): Int =
        when (this) {
            is EmptyDiscountCodeException -> R.string.checkout_discount_required
            is InvalidDiscountCodeException -> R.string.checkout_discount_invalid
            is DiscountLookupException -> R.string.checkout_discount_unavailable
            else -> R.string.checkout_discount_unavailable
        }

    private fun Throwable.toMessageRes(): Int =
        when (this) {
            is EmptyCartCheckoutException -> R.string.checkout_error_empty_cart
            is InvalidCheckoutLineItemException -> R.string.checkout_error_invalid_cart
            is ShopifyCustomerIdUnavailableException -> R.string.checkout_error_customer_id_unavailable
            is MissingCheckoutAddressException -> R.string.checkout_error_no_address
            is CheckoutCartClearException -> R.string.checkout_error_clear_cart
            is CheckoutOrderCreationException -> R.string.checkout_error_order_creation
            else -> R.string.checkout_error_generic
        }

    private companion object {
        const val DEFAULT_CURRENCY_CODE = "USD"
    }
}

private data class PromoState(
    val promoCodeText: String = "",
    val appliedDiscount: CheckoutDiscount? = null,
    val isApplyingDiscount: Boolean = false,
    val discountErrorRes: Int? = null,
)

private data class CheckoutDetailsState(
    val selectedAddressId: Long? = null,
    val deliveryAddress: CheckoutDeliveryAddressUiModel? = null,
    val isLoadingAddress: Boolean = false,
    val paymentMethod: CheckoutPaymentMethodUi = CheckoutPaymentMethodUi.CashOnDelivery,
)
