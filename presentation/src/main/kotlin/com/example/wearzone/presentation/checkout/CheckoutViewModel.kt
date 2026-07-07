package com.example.wearzone.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.checkout.model.CheckoutCartClearException
import com.example.wearzone.domain.checkout.model.CheckoutData
import com.example.wearzone.domain.checkout.model.CheckoutOrderCreationException
import com.example.wearzone.domain.checkout.model.CheckoutPaymentMethod
import com.example.wearzone.domain.checkout.model.CheckoutVariantValidationException
import com.example.wearzone.domain.checkout.model.CustomerInfo
import com.example.wearzone.domain.checkout.model.DiscountLookupException
import com.example.wearzone.domain.checkout.model.EmptyCartCheckoutException
import com.example.wearzone.domain.checkout.model.EmptyDiscountCodeException
import com.example.wearzone.domain.checkout.model.InvalidCheckoutAddressException
import com.example.wearzone.domain.checkout.model.InvalidCheckoutLineItemException
import com.example.wearzone.domain.checkout.model.InvalidDiscountCodeException
import com.example.wearzone.domain.checkout.model.MissingCheckoutAddressException
import com.example.wearzone.domain.checkout.usecase.ApplyDiscountCodeUseCase
import com.example.wearzone.domain.checkout.usecase.CreatePaymentIntentionUseCase
import com.example.wearzone.domain.checkout.usecase.PlaceOrderUseCase
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import com.example.wearzone.domain.customer.address.usecase.CustomerAddressUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val observeCartUseCase: ObserveCartUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val applyDiscountCodeUseCase: ApplyDiscountCodeUseCase,
    private val customerAddressUseCases: CustomerAddressUseCases,
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
    private val createPaymentIntentionUseCase: CreatePaymentIntentionUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val hasCheckoutAccess = MutableStateFlow<Boolean?>(null)
    private val isPlacingOrder = MutableStateFlow(false)
    private val promoState = MutableStateFlow(PromoState())
    private var applyDiscountJob: Job? = null
    private val checkoutDetails = MutableStateFlow(CheckoutDetailsState())

    private val cartItems = observeCartUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList(),
    )

    val uiState = combine(
        cartItems,
        isPlacingOrder,
        promoState,
        checkoutDetails,
        hasCheckoutAccess,
    ) { items, placingOrder, promo, details, hasAccess ->
        when (hasAccess) {
            null -> CheckoutUiState.Loading
            false -> CheckoutUiState.SignInRequired
            true -> buildState(
                items = items,
                placingOrder = placingOrder,
                promoState = promo,
                checkoutDetails = details,
            )
        }
    }.catch { emit(CheckoutUiState.Error(R.string.checkout_error_generic)) }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CheckoutUiState.Loading,
    )

    private val _uiEffect = Channel<CheckoutUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<CheckoutUiEffect> = _uiEffect.receiveAsFlow()

    init {
        verifyCheckoutAccess()
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
            is CheckoutUiIntent.OnPaymentMethodSelected -> onPaymentMethodSelected(intent.method)
            is CheckoutUiIntent.OnPaymobPaymentResult -> onPaymobPaymentResult(
                intent.isSuccess, intent.transactionId, intent.errorMessage
            )

            CheckoutUiIntent.OnPaymentSuccessConfirmed -> confirmPaymobSuccess()
        }
    }

    private fun confirmPaymobSuccess() {
        viewModelScope.launch {

            val paymentId = checkoutDetails.value.pendingPaymentId

            checkoutDetails.value = checkoutDetails.value.copy(
                showPaymentSuccessDialog = false, pendingPaymentId = null
            )

            isPlacingOrder.value = true

            placeOrderInternal(paymentId)
        }
    }

    private fun verifyCheckoutAccess() {
        viewModelScope.launch {
            when (getAuthAccessStateUseCase()) {
                is AuthAccessState.AuthenticatedCustomer -> {
                    hasCheckoutAccess.value = true
                    loadDeliveryAddress()
                }

                AuthAccessState.AuthenticatedMissingCustomerId,
                AuthAccessState.Guest,
                    -> {
                    hasCheckoutAccess.value = false
                }
            }
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
            isProcessingPayment = checkoutDetails.isProcessingPayment,

            showPaymentSuccessDialog = checkoutDetails.showPaymentSuccessDialog,
            pendingPaymentId = checkoutDetails.pendingPaymentId,
        )
    }

    private fun onPaymentMethodSelected(method: CheckoutPaymentMethodUi) {
        checkoutDetails.value = checkoutDetails.value.copy(paymentMethod = method)
    }

    private fun loadDeliveryAddress() {
        viewModelScope.launch {
            if (hasCheckoutAccess.value != true) return@launch

            checkoutDetails.value = checkoutDetails.value.copy(isLoadingAddress = true)

            val customerId = customerAddressUseCases.getCurrentCustomerId().getOrElse {
                checkoutDetails.value = checkoutDetails.value.copy(isLoadingAddress = false)
                return@launch
            }

            customerAddressUseCases.getAddresses(customerId).onSuccess { addresses ->
                val selected = checkoutDetails.value.selectedAddressId?.let { selectedId ->
                    addresses.firstOrNull { it.id == selectedId }
                } ?: addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()

                checkoutDetails.value = checkoutDetails.value.copy(
                    isLoadingAddress = false,
                    selectedAddressId = selected?.id,
                    deliveryAddress = selected?.toDeliveryUiModel(),
                )
            }.onFailure {
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
        applyDiscountJob?.cancel()

        applyDiscountJob = viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) {
                _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_error_empty_cart))
                return@launch
            }

            promoState.value = promoState.value.copy(isApplyingDiscount = true)

            val subtotal = items.sumOf { it.price * it.quantity }

            applyDiscountCodeUseCase(
                code = promoState.value.promoCodeText,
                subtotal = subtotal,
            ).onSuccess { discount ->
                promoState.value = promoState.value.copy(
                    appliedDiscount = discount,
                    discountErrorRes = null,
                    promoCodeText = discount.code,
                    isApplyingDiscount = false,
                )
                _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_discount_applied))
            }.onFailure { error ->
                promoState.value = promoState.value.copy(
                    isApplyingDiscount = false,
                    discountErrorRes = error.toDiscountMessageRes(),
                )
            }
        }
    }

    private fun removeDiscount() {
        applyDiscountJob?.cancel()
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
            if (isPlacingOrder.value || checkoutDetails.value.isProcessingPayment) return@launch

            val items = cartItems.value
            if (items.isEmpty()) return@launch

            if (checkoutDetails.value.paymentMethod == CheckoutPaymentMethodUi.CreditCard) {
                startPaymobPayment()
                return@launch
            }

            isPlacingOrder.value = true
            placeOrderInternal(paymentId = null)
        }
    }

    private suspend fun startPaymobPayment() {
        checkoutDetails.value = checkoutDetails.value.copy(isProcessingPayment = true)

        val items = cartItems.value
        val subtotal = items.sumOf { it.price * it.quantity }
        val discountAmount = promoState.value.appliedDiscount?.calculatedAmount ?: 0.0
        val totalAmount = max(subtotal - discountAmount, 0.0)
        val address = checkoutDetails.value.deliveryAddress
        val user = getCurrentUserUseCase()

        if (address == null || user == null) {
            checkoutDetails.value = checkoutDetails.value.copy(isProcessingPayment = false)
            _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_error_no_address))
            return
        }

        val names = user.displayName?.trim()?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()

        val firstName = names.firstOrNull() ?: "Guest"

        val lastName = names.drop(1).joinToString(" ").ifBlank { "User" }

        val checkoutData = CheckoutData(
            cartItems = items,
            totalAmount = (totalAmount * 100).toLong(),
            customerInfo = CustomerInfo(
                firstName = firstName,
                lastName = lastName,
                email = user.email,
                phone = address.phone ?: "01279336697"
            )
        )

        createPaymentIntentionUseCase(checkoutData).onSuccess { intention ->
                checkoutDetails.value = checkoutDetails.value.copy(isProcessingPayment = false)
                _uiEffect.send(CheckoutUiEffect.NavigateToPaymobSdk(intention.clientSecret))
            }

            .onFailure {
                checkoutDetails.value = checkoutDetails.value.copy(isProcessingPayment = false)
                _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_error_payment_failed))
            }
    }

    private fun onPaymobPaymentResult(
        isSuccess: Boolean,
        transactionId: String?,
        errorMessage: String?,
    ) {
        viewModelScope.launch {

            if (!isSuccess) {
                _uiEffect.send(CheckoutUiEffect.ShowMessage(R.string.checkout_error_payment_failed))
                return@launch
            }

            isPlacingOrder.value = true
            placeOrderInternal(transactionId)
        }
    }

    private suspend fun placeOrderInternal(paymentId: String?) {
        placeOrderUseCase(
            discount = promoState.value.appliedDiscount,
            selectedAddressId = checkoutDetails.value.selectedAddressId,
            paymentMethod = when (checkoutDetails.value.paymentMethod) {
                CheckoutPaymentMethodUi.CashOnDelivery -> CheckoutPaymentMethod.CashOnDelivery
                CheckoutPaymentMethodUi.CreditCard -> CheckoutPaymentMethod.CreditCard
            },
            paymentId = paymentId,
        ).onSuccess {
                isPlacingOrder.value = false
                promoState.value = PromoState()
                _uiEffect.send(CheckoutUiEffect.NavigateToOrderHistory)
            }.onFailure { error ->
                isPlacingOrder.value = false
                _uiEffect.send(CheckoutUiEffect.ShowMessage(error.toMessageRes()))
            }
    }

    private fun retryLoad() {
        viewModelScope.launch {
            promoState.value = promoState.value.copy(discountErrorRes = null)
            verifyCheckoutAccess()
        }
    }

    private fun sendEffect(effect: CheckoutUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun CustomerAddress.toDeliveryUiModel(): CheckoutDeliveryAddressUiModel {
        val recipient =
            name?.takeIf { it.isNotBlank() } ?: listOfNotNull(firstName, lastName).joinToString(" ")
                .ifBlank { "-" }

        val cityParts = listOfNotNull(city, province, zip).filter { it.isNotBlank() }

        return CheckoutDeliveryAddressUiModel(
            id = id,
            recipientName = recipient,
            addressLines = listOfNotNull(
                address1,
                address2,
                cityParts.joinToString(", ").ifBlank { null },
            ).filter { it.isNotBlank() }.joinToString("\n"),
            countryLine = countryName ?: country.orEmpty(),
            phone = this.phone,
        )
    }

    private fun CartItem.toUiModel(): CheckoutCartItemUiModel = CheckoutCartItemUiModel(
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

    private fun CheckoutDeliveryAddressUiModel.cityPart(): String {
        return addressLines.split("\n").lastOrNull() ?: "NA"
    }

    private fun CheckoutDeliveryAddressUiModel.streetPart(): String {
        return addressLines.split("\n").firstOrNull() ?: "NA"
    }

    private fun Throwable.toDiscountMessageRes(): Int = when (this) {
        is EmptyDiscountCodeException -> R.string.checkout_discount_required
        is InvalidDiscountCodeException -> R.string.checkout_discount_invalid
        is DiscountLookupException -> R.string.checkout_discount_unavailable
        else -> R.string.checkout_discount_unavailable
    }

    private fun Throwable.toDetailedCheckoutMessage(): String? = when (this) {
        is InvalidCheckoutLineItemException, is InvalidCheckoutAddressException, is CheckoutVariantValidationException, is CheckoutOrderCreationException -> message?.takeIf { it.isNotBlank() }
        else -> null
    }

    private fun Throwable.toMessageRes(): Int = when (this) {
        is EmptyCartCheckoutException -> R.string.checkout_error_empty_cart
        is InvalidCheckoutLineItemException -> R.string.checkout_error_invalid_cart
        is ShopifyCustomerIdUnavailableException -> R.string.checkout_error_customer_id_unavailable
        is MissingCheckoutAddressException -> R.string.checkout_error_no_address
        is InvalidCheckoutAddressException -> R.string.checkout_error_no_address
        is CheckoutCartClearException -> R.string.checkout_error_clear_cart
        is CheckoutVariantValidationException -> R.string.checkout_error_invalid_cart
        is CheckoutOrderCreationException -> R.string.checkout_error_order_creation
        else -> R.string.checkout_error_generic
    }

    private companion object {
        const val DEFAULT_CURRENCY_CODE = "EG"
    }
}