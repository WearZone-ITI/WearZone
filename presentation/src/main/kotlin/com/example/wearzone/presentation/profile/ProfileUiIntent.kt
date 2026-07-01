package com.example.wearzone.presentation.profile

sealed interface ProfileUiIntent {
    data object OnMyOrdersClicked : ProfileUiIntent
    data object OnWishlistClicked : ProfileUiIntent
    data object OnSavedAddressesClicked : ProfileUiIntent
    data object OnCurrencyClicked : ProfileUiIntent
    data object OnSettingsClicked : ProfileUiIntent
    data object OnLogoutClicked : ProfileUiIntent
    data object OnLogoutConfirmed : ProfileUiIntent
    data object OnLogoutCancelled : ProfileUiIntent
    data object OnCardClicked : ProfileUiIntent
    data object OnRetry : ProfileUiIntent
}
