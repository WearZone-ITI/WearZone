package com.example.wearzone.presentation.address.form

sealed interface AddressFormUiIntent {
    data class OnInitialize(val addressId: Long?) : AddressFormUiIntent
    data object OnBackClicked : AddressFormUiIntent
    data class OnRecipientNameChanged(val value: String) : AddressFormUiIntent
    data class OnCountrySelected(val isoCode: String) : AddressFormUiIntent
    data class OnPhoneChanged(val value: String) : AddressFormUiIntent
    data class OnAddressSearchQueryChanged(val value: String) : AddressFormUiIntent
    data class OnAddressSuggestionSelected(val suggestionId: String) : AddressFormUiIntent
    data class OnCompanyChanged(val value: String) : AddressFormUiIntent
    data class OnAddress1Changed(val value: String) : AddressFormUiIntent
    data class OnAddress2Changed(val value: String) : AddressFormUiIntent
    data class OnCityChanged(val value: String) : AddressFormUiIntent
    data class OnProvinceChanged(val value: String) : AddressFormUiIntent
    data class OnZipChanged(val value: String) : AddressFormUiIntent
    data class OnDefaultChanged(val value: Boolean) : AddressFormUiIntent
    data object OnUseCurrentLocationClicked : AddressFormUiIntent
    data class OnLocationPermissionResult(val isGranted: Boolean) : AddressFormUiIntent
    data object OnPickOnMapClicked : AddressFormUiIntent
    data object OnDismissMapPicker : AddressFormUiIntent
    data object OnMapPickerSearchCleared : AddressFormUiIntent
    data class OnMapPickerCoordinatesChanged(
        val latitude: Double,
        val longitude: Double,
    ) : AddressFormUiIntent
    data object OnConfirmMapLocationClicked : AddressFormUiIntent
    data object OnClearSelectedLocationClicked : AddressFormUiIntent
    data object OnSaveClicked : AddressFormUiIntent
}
