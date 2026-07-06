package com.example.wearzone.presentation.cart

sealed interface CartUiEffect {
    data object NavigateToLogin : CartUiEffect
    data object NavigateToCheckout : CartUiEffect
    data object ShowSignInRequired : CartUiEffect
    data class ShowSnackbar(val message: String) : CartUiEffect
    data class ShowRemoveConfirmation(val variantId: String) : CartUiEffect
    data object ShowClearCartConfirmation : CartUiEffect
}
