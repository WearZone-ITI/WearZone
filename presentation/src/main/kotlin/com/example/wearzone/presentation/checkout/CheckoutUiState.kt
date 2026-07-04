package com.example.wearzone.presentation.checkout

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList

sealed interface CheckoutUiState {
    data object Loading : CheckoutUiState
    data object Empty : CheckoutUiState
    data object SignInRequired : CheckoutUiState

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
    ) : CheckoutUiState

    data class Error(@param:StringRes val messageRes: Int) : CheckoutUiState
}

data class CheckoutCartItemUiModel(
    val variantId: String,
    val title: String,
    val vendor: String,
    val quantity: Int,
    val formattedPrice: String,
    val imageUrl: String?,
)
