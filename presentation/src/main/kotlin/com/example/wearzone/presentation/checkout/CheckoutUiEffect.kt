package com.example.wearzone.presentation.checkout

import androidx.annotation.StringRes

sealed interface CheckoutUiEffect {
    data object NavigateBack : CheckoutUiEffect
    data object NavigateToOrderHistory : CheckoutUiEffect
    data object NavigateToAddressList : CheckoutUiEffect
    data object NavigateToAddAddress : CheckoutUiEffect
    data object ShowConfirmOrderDialog : CheckoutUiEffect
    data class NavigateToPaymobSdk(val clientSecret: String) : CheckoutUiEffect

    data class ShowMessage(@param:StringRes val messageRes: Int) : CheckoutUiEffect
    data class ShowTextMessage(val message: String) : CheckoutUiEffect
}
