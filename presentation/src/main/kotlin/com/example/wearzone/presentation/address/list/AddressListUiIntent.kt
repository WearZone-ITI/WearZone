package com.example.wearzone.presentation.address.list

sealed interface AddressListUiIntent {
    data object OnBackClicked : AddressListUiIntent
    data object OnAddClicked : AddressListUiIntent
    data class OnEditClicked(val addressId: Long) : AddressListUiIntent
    data class OnDeleteClicked(val address: CustomerAddressUiModel) : AddressListUiIntent
    data class OnDeleteConfirmed(val addressId: Long) : AddressListUiIntent
    data class OnSetDefaultClicked(val addressId: Long) : AddressListUiIntent
    data object OnRetry : AddressListUiIntent
    data object OnRefresh : AddressListUiIntent
}
