package com.example.wearzone.presentation.address.form

sealed interface AddressFormUiIntent {
    data class OnInitialize(val addressId: Long?) : AddressFormUiIntent
    data object OnBackClicked : AddressFormUiIntent
    data class OnRecipientNameChanged(val value: String) : AddressFormUiIntent
    data class OnCountryCodeSelected(val value: String) : AddressFormUiIntent
    data class OnPhoneChanged(val value: String) : AddressFormUiIntent
    data class OnCompanyChanged(val value: String) : AddressFormUiIntent
    data class OnAddress1Changed(val value: String) : AddressFormUiIntent
    data class OnAddress2Changed(val value: String) : AddressFormUiIntent
    data class OnCityChanged(val value: String) : AddressFormUiIntent
    data class OnProvinceChanged(val value: String) : AddressFormUiIntent
    data class OnCountryChanged(val value: String) : AddressFormUiIntent
    data class OnZipChanged(val value: String) : AddressFormUiIntent
    data class OnDefaultChanged(val value: Boolean) : AddressFormUiIntent
    data object OnSaveClicked : AddressFormUiIntent
}
