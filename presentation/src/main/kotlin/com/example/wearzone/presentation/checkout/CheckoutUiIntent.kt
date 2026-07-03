package com.example.wearzone.presentation.checkout

sealed interface CheckoutUiIntent {
    data object OnBackClicked : CheckoutUiIntent
    data object OnRetry : CheckoutUiIntent
    data object OnSubmitOrderClicked : CheckoutUiIntent
    data object OnSubmitOrderConfirmed : CheckoutUiIntent
    data class OnPromoCodeChanged(val text: String) : CheckoutUiIntent
    data object OnApplyDiscountClicked : CheckoutUiIntent
    data object OnRemoveDiscountClicked : CheckoutUiIntent
    data object OnChangeAddressClicked : CheckoutUiIntent
    data object OnAddAddressClicked : CheckoutUiIntent
    data object OnRefreshAddresses : CheckoutUiIntent
}
