package com.example.wearzone.presentation.address.list

import androidx.annotation.StringRes

sealed interface AddressListUiEffect {
    data object NavigateBack : AddressListUiEffect
    data object NavigateToAddAddress : AddressListUiEffect
    data class NavigateToEditAddress(val addressId: Long) : AddressListUiEffect
    data class ShowDeleteConfirmation(val addressId: Long) : AddressListUiEffect
    data class ShowMessage(@param:StringRes val messageRes: Int) : AddressListUiEffect
}
