package com.example.wearzone.presentation.address.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.customer.address.model.AddressNetworkException
import com.example.wearzone.domain.customer.address.model.AddressPermissionException
import com.example.wearzone.domain.customer.address.model.AddressResponseParseException
import com.example.wearzone.domain.customer.address.model.CannotDeleteDefaultAddressException
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.model.InvalidShopifyAddressException
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerNotFoundException
import com.example.wearzone.domain.customer.address.usecase.CustomerAddressUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressListViewModel @Inject constructor(
    private val addressUseCases: CustomerAddressUseCases,
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddressListUiState>(AddressListUiState.Loading)
    val uiState: StateFlow<AddressListUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AddressListUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<AddressListUiEffect> = _uiEffect.receiveAsFlow()

    private var customerId: Long? = null

    init {
        loadAddresses()
    }

    fun handleIntent(intent: AddressListUiIntent) {
        when (intent) {
            AddressListUiIntent.OnBackClicked -> sendEffect(AddressListUiEffect.NavigateBack)
            AddressListUiIntent.OnAddClicked -> sendEffect(AddressListUiEffect.NavigateToAddAddress)
            is AddressListUiIntent.OnEditClicked -> {
                sendEffect(AddressListUiEffect.NavigateToEditAddress(intent.addressId))
            }
            is AddressListUiIntent.OnDeleteClicked -> requestDelete(intent.address)
            is AddressListUiIntent.OnDeleteConfirmed -> deleteAddress(intent.addressId)
            is AddressListUiIntent.OnSetDefaultClicked -> setDefaultAddress(intent.addressId)
            AddressListUiIntent.OnRetry -> loadAddresses()
            AddressListUiIntent.OnRefresh -> loadAddresses(isRefresh = true)
        }
    }

    private fun loadAddresses(isRefresh: Boolean = false) {
        viewModelScope.launch {
            setLoadingState(isRefresh)
            val resolvedCustomerId = resolveCustomerId() ?: return@launch

            addressUseCases.getAddresses(resolvedCustomerId)
                .onSuccess { addresses ->
                    _uiState.value = if (addresses.isEmpty()) {
                        AddressListUiState.Empty
                    } else {
                        AddressListUiState.Content(
                            addresses = addresses.map { it.toUiModel() }.toImmutableList(),
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = AddressListUiState.Error(error.toMessageRes())
                }
        }
    }

    private fun setLoadingState(isRefresh: Boolean) {
        val currentState = _uiState.value
        _uiState.value = if (isRefresh && currentState is AddressListUiState.Content) {
            currentState.copy(isRefreshing = true)
        } else {
            AddressListUiState.Loading
        }
    }

    private suspend fun resolveCustomerId(): Long? {
        return when (val accessState = getAuthAccessStateUseCase()) {
            is AuthAccessState.AuthenticatedCustomer -> {
                customerId = accessState.customerId
                accessState.customerId
            }
            AuthAccessState.AuthenticatedMissingCustomerId,
            AuthAccessState.Guest -> {
                customerId = null
                _uiState.value = AddressListUiState.SignInRequired
                null
            }
        }
    }

    private fun requestDelete(address: CustomerAddressUiModel) {
        sendEffect(AddressListUiEffect.ShowDeleteConfirmation(address.id))
    }

    private fun deleteAddress(addressId: Long) {
        viewModelScope.launch {
            val resolvedCustomerId = resolveCustomerId() ?: return@launch
            setMutating(true)
            addressUseCases.deleteAddress(resolvedCustomerId, addressId)
                .onSuccess {
                    sendEffect(AddressListUiEffect.ShowMessage(R.string.address_delete_success))
                    loadAddresses(isRefresh = true)
                }
                .onFailure {
                    setMutating(false)
                    sendEffect(AddressListUiEffect.ShowMessage(it.toMessageRes()))
                }
        }
    }

    private fun setDefaultAddress(addressId: Long) {
        viewModelScope.launch {
            val resolvedCustomerId = resolveCustomerId() ?: return@launch
            setMutating(true)
            addressUseCases.setDefaultAddress(resolvedCustomerId, addressId)
                .onSuccess {
                    sendEffect(AddressListUiEffect.ShowMessage(R.string.address_set_default_success))
                    loadAddresses(isRefresh = true)
                }
                .onFailure {
                    setMutating(false)
                    sendEffect(AddressListUiEffect.ShowMessage(it.toMessageRes()))
                }
        }
    }

    private fun setMutating(isMutating: Boolean) {
        val currentState = _uiState.value as? AddressListUiState.Content ?: return
        _uiState.value = currentState.copy(isMutating = isMutating)
    }

    private fun sendEffect(effect: AddressListUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun CustomerAddress.toUiModel(): CustomerAddressUiModel {
        val recipient = name?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "-" }
        val cityParts = listOfNotNull(city, province, zip).filter { it.isNotBlank() }
        return CustomerAddressUiModel(
            id = id,
            recipientName = recipient,
            phone = phone.orEmpty(),
            addressLine = listOfNotNull(address1, address2).filter { it.isNotBlank() }.joinToString(", "),
            cityLine = cityParts.joinToString(", "),
            countryLine = countryName ?: country.orEmpty(),
            isDefault = isDefault,
        )
    }

    private fun Throwable.toMessageRes(): Int =
        when (this) {
            is CannotDeleteDefaultAddressException ->
                R.string.address_error_cannot_delete_default

            is ShopifyCustomerIdUnavailableException ->
                R.string.address_error_customer_id_unavailable

            is AddressPermissionException ->
                R.string.address_error_permission

            is ShopifyCustomerNotFoundException ->
                R.string.address_error_customer_not_found

            is InvalidShopifyAddressException ->
                R.string.address_error_invalid_shopify_address

            is AddressResponseParseException ->
                R.string.address_error_response_parse

            is AddressNetworkException ->
                R.string.address_error_network

            else ->
                R.string.address_error_generic
        }
}
