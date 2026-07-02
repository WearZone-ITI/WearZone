package com.example.wearzone.presentation.checkout

sealed interface CheckoutUiIntent {
    data object OnBackClicked : CheckoutUiIntent
    data object OnRetry : CheckoutUiIntent
    data object OnSubmitOrderClicked : CheckoutUiIntent
    data object OnSubmitOrderConfirmed : CheckoutUiIntent
}
