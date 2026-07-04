package com.example.wearzone.presentation.order.details

import androidx.annotation.StringRes

sealed interface OrderDetailsUiEffect {
    data object NavigateBack : OrderDetailsUiEffect
    data object NavigateToShopping : OrderDetailsUiEffect
    data object ShowCancelOrderDialog : OrderDetailsUiEffect
    data class ShowMessage(@param:StringRes val messageRes: Int) : OrderDetailsUiEffect
}

