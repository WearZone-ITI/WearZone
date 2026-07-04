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
    data class OnPaymentMethodSelected(val method: CheckoutPaymentMethodUi) : CheckoutUiIntent
    data class OnCardNumberChanged(val number: String) : CheckoutUiIntent
    data class OnCardHolderNameChanged(val firstName: String, val lastName: String) : CheckoutUiIntent
    data class OnCardExpiryChanged(val month: String, val year: String) : CheckoutUiIntent
    data class OnCardCvvChanged(val cvv: String) : CheckoutUiIntent
}
