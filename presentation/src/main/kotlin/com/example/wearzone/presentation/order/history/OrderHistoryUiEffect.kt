package com.example.wearzone.presentation.order.history

import androidx.annotation.StringRes

sealed interface OrderHistoryUiEffect {
    data object NavigateBack : OrderHistoryUiEffect
    data class ShowMessage(@param:StringRes val messageRes: Int) : OrderHistoryUiEffect
}
