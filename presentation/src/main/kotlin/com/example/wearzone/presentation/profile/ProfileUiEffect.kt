package com.example.wearzone.presentation.profile

import androidx.annotation.StringRes

sealed interface ProfileUiEffect {
    data object NavigateToLogin : ProfileUiEffect
    data object NavigateToSettings : ProfileUiEffect
    data object NavigateToWishlist : ProfileUiEffect
    data object NavigateToOrders : ProfileUiEffect
    data object NavigateToSavedAddresses : ProfileUiEffect
    data object ShowLogoutConfirmation : ProfileUiEffect
    data class ShowError(@param:StringRes val messageRes: Int) : ProfileUiEffect
}
