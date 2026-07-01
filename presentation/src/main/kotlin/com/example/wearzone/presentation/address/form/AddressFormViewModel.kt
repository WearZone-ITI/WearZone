package com.example.wearzone.presentation.address.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.customer.address.model.AddressInput
import com.example.wearzone.domain.customer.address.model.AddressNetworkException
import com.example.wearzone.domain.customer.address.model.AddressPermissionException
import com.example.wearzone.domain.customer.address.model.AddressResponseParseException
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.model.InvalidShopifyAddressException
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerNotFoundException
import com.example.wearzone.domain.customer.address.usecase.CustomerAddressUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressFormViewModel @Inject constructor(
    private val addressUseCases: CustomerAddressUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressFormUiState(isLoading = true))
    val uiState: StateFlow<AddressFormUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AddressFormUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<AddressFormUiEffect> = _uiEffect.receiveAsFlow()

    private var initializedAddressId: Long? = null
    private var hasInitialized = false
    private var customerId: Long? = null

    fun handleIntent(intent: AddressFormUiIntent) {
        when (intent) {
            is AddressFormUiIntent.OnInitialize -> initialize(intent.addressId)
            AddressFormUiIntent.OnBackClicked -> sendEffect(AddressFormUiEffect.NavigateBack)
            is AddressFormUiIntent.OnRecipientNameChanged -> updateField { copy(recipientName = intent.value) }
            is AddressFormUiIntent.OnCountryCodeSelected -> updateField {
                copy(countryCode = intent.value)
            }
            is AddressFormUiIntent.OnPhoneChanged -> updateField { copy(phone = intent.value) }
            is AddressFormUiIntent.OnCompanyChanged -> updateField { copy(company = intent.value) }
            is AddressFormUiIntent.OnAddress1Changed -> updateField { copy(address1 = intent.value) }
            is AddressFormUiIntent.OnAddress2Changed -> updateField { copy(address2 = intent.value) }
            is AddressFormUiIntent.OnCityChanged -> updateField { copy(city = intent.value) }
            is AddressFormUiIntent.OnProvinceChanged -> updateField { copy(province = intent.value) }
            is AddressFormUiIntent.OnCountryChanged -> updateField { copy(country = intent.value) }
            is AddressFormUiIntent.OnZipChanged -> updateField { copy(zip = intent.value) }
            is AddressFormUiIntent.OnDefaultChanged -> updateField { copy(isDefault = intent.value) }
            AddressFormUiIntent.OnSaveClicked -> saveAddress()
        }
    }

    private fun initialize(addressId: Long?) {
        if (hasInitialized && initializedAddressId == addressId) return
        hasInitialized = true
        initializedAddressId = addressId

        if (addressId == null) {
            _uiState.value = AddressFormUiState(isLoading = false).withValidation(showRequiredErrors = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = AddressFormUiState(addressId = addressId, isLoading = true)
            val resolvedCustomerId = resolveCustomerId()
            if (resolvedCustomerId == null) {
                _uiState.update { it.copy(isSubmitting = false) }
                return@launch
            }
            addressUseCases.getAddress(resolvedCustomerId, addressId)
                .onSuccess { address -> _uiState.value = address.toFormState() }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, screenError = error.toMessageRes())
                    }
                }
        }
    }

    private fun saveAddress() {
        val validatedState = _uiState.value
            .withValidation(showRequiredErrors = true)
            .copy(isValidationVisible = true)
        _uiState.value = validatedState
        if (!validatedState.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, screenError = null) }
            val resolvedCustomerId = resolveCustomerId() ?: return@launch
            val currentState = _uiState.value
            val input = currentState.toAddressInput()
            val result = currentState.addressId?.let { addressId ->
                addressUseCases.updateAddress(resolvedCustomerId, addressId, input)
            } ?: addressUseCases.createAddress(resolvedCustomerId, input)

            result
                .onSuccess { savedAddress -> handleSaveSuccess(resolvedCustomerId, savedAddress) }
                .onFailure { error -> handleSaveFailure(error) }
        }
    }

    private suspend fun handleSaveSuccess(customerId: Long, savedAddress: CustomerAddress) {
        val shouldSetDefault = _uiState.value.isDefault
        if (shouldSetDefault) {
            addressUseCases.setDefaultAddress(customerId, savedAddress.id)
                .onFailure { error ->
                    handleSaveFailure(error)
                    return
            }
        }
        _uiEffect.send(AddressFormUiEffect.AddressSaved)
        _uiEffect.send(AddressFormUiEffect.ShowMessage(R.string.address_save_success))
        _uiEffect.send(AddressFormUiEffect.NavigateBack)
    }

    private fun handleSaveFailure(error: Throwable) {
        _uiState.update {
            it.copy(
                isSubmitting = false,
                screenError = error.toMessageRes(),
            )
        }
    }

    private suspend fun resolveCustomerId(): Long? {
        customerId?.let { return it }
        return addressUseCases.getCurrentCustomerId()
            .onSuccess { customerId = it }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSubmitting = false,
                        screenError = error.toMessageRes(),
                    )
                }
            }
            .getOrNull()
    }

    private fun updateField(reducer: AddressFormUiState.() -> AddressFormUiState) {
        _uiState.update { state ->
            state.reducer()
                .copy(screenError = null)
                .withValidation(showRequiredErrors = state.isValidationVisible)
        }
    }

    private fun sendEffect(effect: AddressFormUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun AddressFormUiState.withValidation(showRequiredErrors: Boolean): AddressFormUiState =
        copy(
            recipientNameError = recipientNameError(recipientName, showRequiredErrors),
            phoneError = phoneError(countryCode, phone, showRequiredErrors),
            address1Error = requiredTextError(address1, showRequiredErrors),
            cityError = requiredTextError(city, showRequiredErrors),
            countryError = countryError(country, showRequiredErrors),
            zipError = zipError(zip, showRequiredErrors),
        )

    private fun recipientNameError(value: String, showRequiredErrors: Boolean): Int? {
        val trimmedValue = value.trim()
        return when {
            trimmedValue.isBlank() -> requiredError(showRequiredErrors)
            else -> null
        }
    }

    private fun phoneError(countryCode: String, value: String, showRequiredErrors: Boolean): Int? =
        when {
            value.isBlank() -> requiredError(showRequiredErrors)
            countryCode != EGYPT_COUNTRY_CODE -> R.string.address_error_unsupported_country_code
            !value.isValidPhoneNumber() -> R.string.address_error_invalid_phone
            else -> null
        }

    private fun requiredTextError(value: String, showRequiredErrors: Boolean): Int? =
        if (value.isBlank()) requiredError(showRequiredErrors) else null

    private fun countryError(value: String, showRequiredErrors: Boolean): Int? =
        when {
            value.isBlank() -> requiredError(showRequiredErrors)
            else -> null
        }

    private fun zipError(value: String, showRequiredErrors: Boolean): Int? =
        when {
            value.isBlank() -> null
            value.any { !it.isDigit() } -> R.string.address_error_invalid_postal_code
            else -> null
        }

    private fun requiredError(showRequiredErrors: Boolean): Int? =
        if (showRequiredErrors) R.string.address_error_required_field else null

    private fun AddressFormUiState.toAddressInput(): AddressInput {
        val nameParts = recipientName.trim().split(NAME_SPLIT_REGEX, limit = 2)
        return AddressInput(
            firstName = nameParts.firstOrNull().orEmpty(),
            lastName = nameParts.getOrNull(1).orEmpty(),
            company = company.toOptionalString(),
            address1 = address1.trim(),
            address2 = address2.toOptionalString(),
            city = city.trim(),
            province = province.toOptionalString(),
            country = country.trim(),
            zip = zip.trim(),
            phone = phone.toShopifyPhoneNumber(),
        )
    }

    private fun CustomerAddress.toFormState(): AddressFormUiState =
        AddressFormUiState(
            addressId = id,
            isLoading = false,
            recipientName = name?.takeIf { it.isNotBlank() }
                ?: listOfNotNull(firstName, lastName).joinToString(" "),
            countryCode = EGYPT_COUNTRY_CODE,
            phone = phone.orEmpty().toEgyptLocalPhoneInput(),
            company = company.orEmpty(),
            address1 = address1.orEmpty(),
            address2 = address2.orEmpty(),
            city = city.orEmpty(),
            province = province.orEmpty(),
            country = countryName ?: country.orEmpty(),
            zip = zip.orEmpty(),
            isDefault = isDefault,
        ).withValidation(showRequiredErrors = false)

    private fun String.toOptionalString(): String? = trim().takeIf { it.isNotBlank() }

    private fun String.toEgyptLocalPhoneInput(): String {
        val digits = filter(Char::isDigit)
        val withoutInternationalPrefix = when {
            digits.startsWith(EGYPT_DOUBLE_ZERO_PREFIX) -> digits.removePrefix(EGYPT_DOUBLE_ZERO_PREFIX)
            digits.startsWith(EGYPT_DIAL_PREFIX) && digits.length > EGYPT_LOCAL_PHONE_LENGTH_WITH_ZERO ->
                digits.removePrefix(EGYPT_DIAL_PREFIX)
            else -> digits
        }
        return withoutInternationalPrefix.take(EGYPT_LOCAL_PHONE_LENGTH_WITH_ZERO)
    }

    private fun String.isValidPhoneNumber(): Boolean =
        length == PHONE_LENGTH && all(Char::isDigit)

    private fun String.toShopifyPhoneNumber(): String {
        return "$EGYPT_COUNTRY_CODE$this"
    }

    private fun Throwable.toMessageRes(): Int =
        when (this) {
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

    private companion object {
        val NAME_SPLIT_REGEX = "\\s+".toRegex()
        const val EGYPT_COUNTRY = "Egypt"
        const val EGYPT_COUNTRY_CODE = "+20"
        const val EGYPT_DOUBLE_ZERO_PREFIX = "0020"
        const val EGYPT_DIAL_PREFIX = "20"
        const val EGYPT_LOCAL_PHONE_LENGTH_WITH_ZERO = 11
        const val PHONE_LENGTH = 11
    }
}
