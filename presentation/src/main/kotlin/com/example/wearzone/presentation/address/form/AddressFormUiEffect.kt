package com.example.wearzone.presentation.address.form

import androidx.annotation.StringRes

sealed interface AddressFormUiEffect {
    data object NavigateBack : AddressFormUiEffect
    data object AddressSaved : AddressFormUiEffect
    data class ShowMessage(@param:StringRes val messageRes: Int) : AddressFormUiEffect
}
