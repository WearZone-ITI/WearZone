package com.example.wearzone.presentation.checkout

import androidx.annotation.StringRes
import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import kotlinx.collections.immutable.ImmutableList

sealed interface CheckoutUiState {
    data object Loading : CheckoutUiState
    data object Empty : CheckoutUiState

    data class Content(
        val items: ImmutableList<CheckoutCartItemUiModel>,
        val itemCount: Int,
        val subtotal: String,
        val formattedDiscount: String?,
        val total: String,
        val promoCodeText: String,
        val appliedDiscountCode: String?,
        @param:StringRes val discountErrorRes: Int?,
        val isApplyingDiscount: Boolean,
        val isPlacingOrder: Boolean = false,
        val deliveryAddress: CheckoutDeliveryAddressUiModel? = null,
        val isLoadingAddress: Boolean = false,
        val paymentMethod: CheckoutPaymentMethodUi = CheckoutPaymentMethodUi.CashOnDelivery,
        val cardInfo: CardInfoUiModel = CardInfoUiModel(),
        val isProcessingPayment: Boolean = false,
    ) : CheckoutUiState

    data class Error(@param:StringRes val messageRes: Int) : CheckoutUiState
}

data class CardInfoUiModel(
    val number: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val month: String = "",
    val year: String = "",
    val cvv: String = "",

    @StringRes val numberError: Int? = null,
    @StringRes val firstNameError: Int? = null,
    @StringRes val lastNameError: Int? = null,
    @StringRes val monthError: Int? = null,
    @StringRes val yearError: Int? = null,
    @StringRes val cvvError: Int? = null,
){
    fun hasErrors(): Boolean {
        return listOf(
            numberError,
            firstNameError,
            lastNameError,
            monthError,
            yearError,
            cvvError
        ).any { it != null }
    }
}

data class CheckoutCartItemUiModel(
    val variantId: String,
    val title: String,
    val vendor: String,
    val quantity: Int,
    val formattedPrice: String,
    val imageUrl: String?,
)

data class PromoState(
    val promoCodeText: String = "",
    val appliedDiscount: CheckoutDiscount? = null,
    val isApplyingDiscount: Boolean = false,
    val discountErrorRes: Int? = null,
)

data class CheckoutDetailsState(
    val selectedAddressId: Long? = null,
    val deliveryAddress: CheckoutDeliveryAddressUiModel? = null,
    val isLoadingAddress: Boolean = false,
    val paymentMethod: CheckoutPaymentMethodUi = CheckoutPaymentMethodUi.CashOnDelivery,
    val cardInfo: CardInfoUiModel = CardInfoUiModel(),
    val isProcessingPayment: Boolean = false,
)
