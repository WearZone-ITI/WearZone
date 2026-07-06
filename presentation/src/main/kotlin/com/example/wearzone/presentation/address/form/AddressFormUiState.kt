package com.example.wearzone.presentation.address.form

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AddressFormUiState(
    val addressId: Long? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSignInRequired: Boolean = false,
    val countries: ImmutableList<CountryUiModel> = persistentListOf(),
    val addressSearchQuery: String = "",
    val addressSuggestions: ImmutableList<AddressSuggestionUiModel> = persistentListOf(),
    val isSearchingAddress: Boolean = false,
    val isResolvingCurrentLocation: Boolean = false,
    val isMapPickerVisible: Boolean = false,
    val isResolvingMapLocation: Boolean = false,
    val mapPickerLatitude: Double? = null,
    val mapPickerLongitude: Double? = null,
    val mapPickerCurrentLatitude: Double? = null,
    val mapPickerCurrentLongitude: Double? = null,
    val mapPickerSelectedLocation: SelectedLocationUiModel? = null,
    val mapPickerSelectedSuggestion: AddressSuggestionUiModel? = null,
    val selectedLocation: SelectedLocationUiModel? = null,
    val recipientName: String = "",
    val countryIsoCode: String = "EG",
    val countryDialCode: String = "+20",
    val phone: String = "",
    val company: String = "",
    val address1: String = "",
    val address2: String = "",
    val city: String = "",
    val province: String = "",
    val country: String = "Egypt",
    val zip: String = "",
    val isDefault: Boolean = false,
    val isValidationVisible: Boolean = false,
    @param:StringRes val recipientNameError: Int? = null,
    @param:StringRes val phoneError: Int? = null,
    @param:StringRes val address1Error: Int? = null,
    @param:StringRes val cityError: Int? = null,
    @param:StringRes val countryError: Int? = null,
    @param:StringRes val zipError: Int? = null,
    @param:StringRes val addressLookupMessage: Int? = null,
    @param:StringRes val screenError: Int? = null,
) {
    val isEditMode: Boolean = addressId != null
    val canSubmit: Boolean =
        !isLoading &&
            !isSubmitting &&
            !isSignInRequired &&
            recipientNameError == null &&
            phoneError == null &&
            address1Error == null &&
            cityError == null &&
            countryError == null &&
            zipError == null &&
            recipientName.isNotBlank() &&
            phone.isNotBlank() &&
            address1.isNotBlank() &&
            city.isNotBlank() &&
            country.isNotBlank()

    val selectedCountry: CountryUiModel?
        get() = countries.firstOrNull { it.isoCode == countryIsoCode }
}

data class CountryUiModel(
    val name: String,
    val isoCode: String,
    val dialCode: String,
    val minNationalNumberLength: Int,
    val maxNationalNumberLength: Int,
    val exampleNationalNumber: String,
    val trunkPrefix: String?,
)

data class AddressSuggestionUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val address1: String,
    val city: String?,
    val province: String?,
    val countryName: String?,
    val countryCode: String?,
    val postalCode: String?,
    val latitude: Double?,
    val longitude: Double?,
)

data class SelectedLocationUiModel(
    val title: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
)
