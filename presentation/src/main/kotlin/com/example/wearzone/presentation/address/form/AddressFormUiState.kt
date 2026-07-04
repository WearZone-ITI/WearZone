package com.example.wearzone.presentation.address.form

import androidx.annotation.StringRes

data class AddressFormUiState(
    val addressId: Long? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSignInRequired: Boolean = false,
    val recipientName: String = "",
    val countryCode: String = "+20",
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
    @param:StringRes val screenError: Int? = null,
) {
    val isEditMode: Boolean = addressId != null
    val canSubmit: Boolean =
        !isLoading &&
            !isSubmitting &&
            !isSignInRequired &&
            screenError == null &&
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
}
