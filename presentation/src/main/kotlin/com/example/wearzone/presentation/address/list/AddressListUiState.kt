package com.example.wearzone.presentation.address.list

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList

sealed interface AddressListUiState {
    data object Loading : AddressListUiState
    data object Empty : AddressListUiState
    data object SignInRequired : AddressListUiState

    data class Content(
        val addresses: ImmutableList<CustomerAddressUiModel>,
        val isRefreshing: Boolean = false,
        val isMutating: Boolean = false,
    ) : AddressListUiState

    data class Error(@param:StringRes val messageRes: Int) : AddressListUiState
}

data class CustomerAddressUiModel(
    val id: Long,
    val recipientName: String,
    val phone: String,
    val addressLine: String,
    val cityLine: String,
    val countryLine: String,
    val isDefault: Boolean,
)
