package com.example.wearzone.presentation.checkout

import androidx.annotation.StringRes

sealed interface CheckoutUiEffect {
    data object NavigateBack : CheckoutUiEffect
    data object NavigateToOrderHistory : CheckoutUiEffect
    data object ShowConfirmOrderDialog : CheckoutUiEffect
    data class ShowMessage(@param:StringRes val messageRes: Int) : CheckoutUiEffect
}
