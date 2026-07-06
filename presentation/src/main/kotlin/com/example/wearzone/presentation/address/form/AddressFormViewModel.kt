package com.example.wearzone.presentation.address.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.usecase.CustomerAddressUseCases
import com.example.wearzone.domain.customer.address.usecase.GetCountriesUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCurrentAddressCoordinatesUseCase
import com.example.wearzone.domain.customer.address.usecase.ReverseGeocodeAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.SearchAddressSuggestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddressFormViewModel @Inject constructor(
    private val addressUseCases: CustomerAddressUseCases,
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
    private val getCountriesUseCase: GetCountriesUseCase,
    private val searchAddressSuggestionsUseCase: SearchAddressSuggestionsUseCase,
    private val reverseGeocodeAddressUseCase: ReverseGeocodeAddressUseCase,
    private val getCurrentAddressCoordinatesUseCase: GetCurrentAddressCoordinatesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressFormUiState(isLoading = true))
    val uiState: StateFlow<AddressFormUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AddressFormUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<AddressFormUiEffect> = _uiEffect.receiveAsFlow()

    private var initializedAddressId: Long? = null
    private var hasInitialized = false
    private var customerId: Long? = null
    private var addressSearchJob: Job? = null
    private var mapReverseGeocodeJob: Job? = null

    fun handleIntent(intent: AddressFormUiIntent) {
        when (intent) {
            is AddressFormUiIntent.OnInitialize -> initialize(intent.addressId)
            AddressFormUiIntent.OnBackClicked -> sendEffect(AddressFormUiEffect.NavigateBack)
            is AddressFormUiIntent.OnRecipientNameChanged -> updateField { copy(recipientName = intent.value) }
            is AddressFormUiIntent.OnCountrySelected -> selectCountry(intent.isoCode)
            is AddressFormUiIntent.OnPhoneChanged -> updatePhone(intent.value)
            is AddressFormUiIntent.OnAddressSearchQueryChanged -> onAddressSearchQueryChanged(intent.value)
            is AddressFormUiIntent.OnAddressSuggestionSelected -> selectSuggestion(intent.suggestionId)
            is AddressFormUiIntent.OnCompanyChanged -> updateField { copy(company = intent.value) }
            is AddressFormUiIntent.OnAddress1Changed -> updateField { copy(address1 = intent.value) }
            is AddressFormUiIntent.OnAddress2Changed -> updateField { copy(address2 = intent.value) }
            is AddressFormUiIntent.OnCityChanged -> updateField { copy(city = intent.value) }
            is AddressFormUiIntent.OnProvinceChanged -> updateField { copy(province = intent.value) }
            is AddressFormUiIntent.OnZipChanged -> updateField { copy(zip = intent.value) }
            is AddressFormUiIntent.OnDefaultChanged -> updateField { copy(isDefault = intent.value) }
            AddressFormUiIntent.OnUseCurrentLocationClicked ->
                sendEffect(AddressFormUiEffect.RequestLocationPermission)
            is AddressFormUiIntent.OnLocationPermissionResult ->
                onLocationPermissionResult(intent.isGranted)
            AddressFormUiIntent.OnPickOnMapClicked -> showMapPicker()
            AddressFormUiIntent.OnDismissMapPicker -> hideMapPicker()
            AddressFormUiIntent.OnMapPickerSearchCleared -> clearMapPickerSearch()
            is AddressFormUiIntent.OnMapPickerCoordinatesChanged ->
                updateMapPickerCoordinates(intent.latitude, intent.longitude)
            AddressFormUiIntent.OnConfirmMapLocationClicked -> confirmMapLocation()
            AddressFormUiIntent.OnClearSelectedLocationClicked -> clearSelectedLocation()
            AddressFormUiIntent.OnSaveClicked -> saveAddress()
        }
    }

    private fun initialize(addressId: Long?) {
        if (hasInitialized && initializedAddressId == addressId) return
        hasInitialized = true
        initializedAddressId = addressId

        viewModelScope.launch {
            val countries = loadCountries()
            _uiState.value = AddressFormUiState(
                addressId = addressId,
                isLoading = true,
                countries = countries,
            ).withDefaultCountry()

            val resolvedCustomerId = resolveCustomerId() ?: return@launch
            if (addressId == null) {
                _uiState.update { state ->
                    state.copy(isLoading = false).withValidation(showRequiredErrors = false)
                }
                return@launch
            }

            addressUseCases.getAddress(resolvedCustomerId, addressId)
                .onSuccess { address -> _uiState.value = address.toFormState(countries) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, screenError = error.toAddressMessageRes())
                    }
                }
        }
    }

    private suspend fun loadCountries(): ImmutableList<CountryUiModel> =
        getCountriesUseCase()
            .getOrDefault(emptyList())
            .map { it.toUiModel() }
            .ifEmpty { listOf(FALLBACK_COUNTRY) }
            .toImmutableList()

    private fun selectCountry(isoCode: String) {
        updateField {
            val country = countries.findCountry(isoCode) ?: selectedCountry ?: FALLBACK_COUNTRY
            copy(
                countryIsoCode = country.isoCode,
                countryDialCode = country.dialCode,
                country = country.name,
                phone = phone.toNationalPhoneInput(country),
            )
        }
    }

    private fun updatePhone(value: String) {
        updateField {
            val country = selectedCountry ?: FALLBACK_COUNTRY
            copy(phone = value.toNationalPhoneInput(country))
        }
    }

    private fun onAddressSearchQueryChanged(value: String) {
        addressSearchJob?.cancel()
        _uiState.update {
            it.copy(
                addressSearchQuery = value,
                addressLookupMessage = null,
                addressSuggestions = persistentListOf(),
                isSearchingAddress = false,
            )
        }
        if (value.trim().length < MIN_ADDRESS_SEARCH_LENGTH) return

        addressSearchJob = viewModelScope.launch {
            delay(ADDRESS_SEARCH_DEBOUNCE_MILLIS)
            val state = _uiState.value
            val countryIsoCode = state.countryIsoCode.takeUnless { state.isMapPickerVisible }
            _uiState.update { it.copy(isSearchingAddress = true, addressLookupMessage = null) }
            searchAddressSuggestionsUseCase(value, countryIsoCode)
                .onSuccess { suggestions ->
                    _uiState.update {
                        it.copy(
                            isSearchingAddress = false,
                            addressSuggestions = suggestions.map { suggestion ->
                                suggestion.toUiModel()
                            }.toImmutableList(),
                            addressLookupMessage = if (suggestions.isEmpty()) {
                                R.string.address_lookup_no_results
                            } else {
                                null
                            },
                        )
                    }
                }
                .onFailure { error -> handleLookupFailure(error) }
        }
    }

    private fun selectSuggestion(suggestionId: String) {
        val suggestion = _uiState.value.addressSuggestions.firstOrNull { it.id == suggestionId }
            ?: return
        if (_uiState.value.isMapPickerVisible) {
            selectMapPickerSuggestion(suggestion)
        } else {
            applySuggestion(suggestion, messageRes = R.string.address_lookup_applied)
        }
    }

    private fun onLocationPermissionResult(isGranted: Boolean) {
        if (!isGranted) {
            if (_uiState.value.isMapPickerVisible) {
                _uiState.update {
                    it.copy(
                        isResolvingCurrentLocation = false,
                        addressLookupMessage = R.string.address_location_permission_denied,
                    )
                }
            } else {
                sendEffect(AddressFormUiEffect.ShowMessage(R.string.address_location_permission_denied))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isResolvingCurrentLocation = true, addressLookupMessage = null)
            }
            getCurrentAddressCoordinatesUseCase()
                .onSuccess { coordinates ->
                    _uiState.update {
                        it.copy(
                            mapPickerCurrentLatitude = coordinates.latitude,
                            mapPickerCurrentLongitude = coordinates.longitude,
                        )
                    }
                    reverseGeocodeAddressUseCase(coordinates)
                        .onSuccess { suggestion ->
                            val suggestionUiModel = suggestion?.toUiModel()
                            if (_uiState.value.isMapPickerVisible) {
                                if (suggestionUiModel == null) {
                                    setMapPickerCoordinateSelection(
                                        latitude = coordinates.latitude,
                                        longitude = coordinates.longitude,
                                        messageRes = R.string.address_location_no_result,
                                    )
                                } else {
                                    setMapPickerPendingSuggestion(
                                        suggestion = suggestionUiModel.withCoordinates(
                                            coordinates.latitude,
                                            coordinates.longitude,
                                        ),
                                        messageRes = R.string.address_current_location_applied,
                                    )
                                }
                            } else if (suggestionUiModel == null) {
                                _uiState.update {
                                    it.copy(
                                        isResolvingCurrentLocation = false,
                                        addressLookupMessage = R.string.address_location_no_result,
                                    )
                                }
                            } else {
                                applySuggestion(
                                    suggestionUiModel,
                                    messageRes = R.string.address_current_location_applied,
                                )
                            }
                        }
                        .onFailure { error ->
                            if (_uiState.value.isMapPickerVisible) {
                                setMapPickerCoordinateSelection(
                                    latitude = coordinates.latitude,
                                    longitude = coordinates.longitude,
                                    messageRes = error.toLookupMessageRes(),
                                )
                            } else {
                                handleLookupFailure(error)
                            }
                        }
                }
                .onFailure { error -> handleLookupFailure(error) }
        }
    }

    private fun showMapPicker() {
        val state = _uiState.value
        val latitude = state.selectedLocation?.latitude ?: state.mapPickerCurrentLatitude ?: DEFAULT_MAP_LATITUDE
        val longitude = state.selectedLocation?.longitude ?: state.mapPickerCurrentLongitude ?: DEFAULT_MAP_LONGITUDE
        _uiState.update {
            it.copy(
                isMapPickerVisible = true,
                mapPickerLatitude = latitude,
                mapPickerLongitude = longitude,
                mapPickerSelectedLocation = state.selectedLocation,
                mapPickerSelectedSuggestion = null,
                addressSearchQuery = "",
                addressSuggestions = persistentListOf(),
                addressLookupMessage = null,
            )
        }
    }

    private fun hideMapPicker() {
        addressSearchJob?.cancel()
        mapReverseGeocodeJob?.cancel()
        _uiState.update {
            it.copy(
                isMapPickerVisible = false,
                isResolvingMapLocation = false,
                isSearchingAddress = false,
                addressSearchQuery = "",
                addressSuggestions = persistentListOf(),
                addressLookupMessage = null,
            )
        }
    }

    private fun clearMapPickerSearch() {
        addressSearchJob?.cancel()
        _uiState.update {
            it.copy(
                addressSearchQuery = "",
                addressSuggestions = persistentListOf(),
                isSearchingAddress = false,
                addressLookupMessage = null,
            )
        }
    }

    private fun updateMapPickerCoordinates(latitude: Double, longitude: Double) {
        val clampedLatitude = latitude.coerceIn(MIN_LATITUDE, MAX_LATITUDE)
        val clampedLongitude = longitude.coerceIn(MIN_LONGITUDE, MAX_LONGITUDE)
        val currentState = _uiState.value
        val pendingSuggestion = currentState.mapPickerSelectedSuggestion
        val shouldKeepPendingSuggestion = pendingSuggestion?.isAtCoordinates(
            latitude = clampedLatitude,
            longitude = clampedLongitude,
        ) == true

        if (shouldKeepPendingSuggestion) {
            _uiState.update {
                it.copy(
                    mapPickerLatitude = clampedLatitude,
                    mapPickerLongitude = clampedLongitude,
                )
            }
            return
        }

        setMapPickerCoordinateSelection(
            latitude = clampedLatitude,
            longitude = clampedLongitude,
            messageRes = null,
        )
        scheduleMapReverseGeocode(clampedLatitude, clampedLongitude)
    }

    private fun confirmMapLocation() {
        val latitude = _uiState.value.mapPickerLatitude ?: DEFAULT_MAP_LATITUDE
        val longitude = _uiState.value.mapPickerLongitude ?: DEFAULT_MAP_LONGITUDE
        val pendingSuggestion = _uiState.value.mapPickerSelectedSuggestion
        if (pendingSuggestion != null) {
            applySuggestion(
                suggestion = pendingSuggestion.withCoordinates(latitude, longitude),
                messageRes = R.string.address_map_location_applied,
                closeMapPicker = true,
            )
            return
        }
        val coordinates = AddressCoordinates(latitude = latitude, longitude = longitude)
        viewModelScope.launch {
            _uiState.update { it.copy(isResolvingMapLocation = true, addressLookupMessage = null) }
            reverseGeocodeAddressUseCase(coordinates)
                .onSuccess { suggestion ->
                    val suggestionUiModel = suggestion?.toUiModel()
                    if (suggestionUiModel == null) {
                        _uiState.update {
                            it.copy(
                                isMapPickerVisible = false,
                                isResolvingMapLocation = false,
                                selectedLocation = SelectedLocationUiModel(
                                    title = "${latitude.formatCoordinate()}, ${longitude.formatCoordinate()}",
                                    subtitle = "",
                                    latitude = latitude,
                                    longitude = longitude,
                                ),
                                addressLookupMessage = R.string.address_location_no_result,
                            )
                        }
                    } else {
                        applySuggestion(
                            suggestion = suggestionUiModel.withCoordinates(latitude, longitude),
                            messageRes = R.string.address_map_location_applied,
                            closeMapPicker = true,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isMapPickerVisible = false,
                            isResolvingMapLocation = false,
                            selectedLocation = SelectedLocationUiModel(
                                title = "${latitude.formatCoordinate()}, ${longitude.formatCoordinate()}",
                                subtitle = "",
                                latitude = latitude,
                                longitude = longitude,
                            ),
                            addressLookupMessage = error.toLookupMessageRes(),
                        )
                    }
                }
        }
    }

    private fun clearSelectedLocation() {
        _uiState.update {
            it.copy(
                selectedLocation = null,
                mapPickerSelectedLocation = null,
                mapPickerSelectedSuggestion = null,
                mapPickerLatitude = null,
                mapPickerLongitude = null,
                mapPickerCurrentLatitude = null,
                mapPickerCurrentLongitude = null,
                addressLookupMessage = null,
            )
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
        if (_uiState.value.isDefault) {
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
                screenError = error.toAddressMessageRes(),
            )
        }
    }

    private fun handleLookupFailure(error: Throwable) {
        _uiState.update {
            it.copy(
                isSearchingAddress = false,
                isResolvingCurrentLocation = false,
                isResolvingMapLocation = false,
                addressLookupMessage = error.toLookupMessageRes(),
            )
        }
    }

    private fun selectMapPickerSuggestion(suggestion: AddressSuggestionUiModel) {
        val latitude = suggestion.latitude ?: _uiState.value.mapPickerLatitude ?: DEFAULT_MAP_LATITUDE
        val longitude = suggestion.longitude ?: _uiState.value.mapPickerLongitude ?: DEFAULT_MAP_LONGITUDE
        setMapPickerPendingSuggestion(
            suggestion = suggestion.withCoordinates(latitude, longitude),
            messageRes = R.string.address_lookup_applied,
        )
    }

    private fun setMapPickerPendingSuggestion(
        suggestion: AddressSuggestionUiModel,
        messageRes: Int,
    ) {
        val latitude = suggestion.latitude ?: _uiState.value.mapPickerLatitude ?: DEFAULT_MAP_LATITUDE
        val longitude = suggestion.longitude ?: _uiState.value.mapPickerLongitude ?: DEFAULT_MAP_LONGITUDE
        mapReverseGeocodeJob?.cancel()
        _uiState.update { state ->
            val suggestedCountry = suggestion.countryCode?.let { state.countries.findCountry(it) }
            val country = suggestedCountry ?: state.selectedCountry ?: FALLBACK_COUNTRY
            state.copy(
                mapPickerLatitude = latitude,
                mapPickerLongitude = longitude,
                mapPickerSelectedSuggestion = suggestion.withCoordinates(latitude, longitude),
                mapPickerSelectedLocation = SelectedLocationUiModel(
                    title = suggestion.title,
                    subtitle = suggestion.subtitle,
                    latitude = latitude,
                    longitude = longitude,
                ),
                country = country.name,
                countryIsoCode = country.isoCode,
                countryDialCode = country.dialCode,
                phone = state.phone.toNationalPhoneInput(country),
                addressSearchQuery = "",
                addressSuggestions = persistentListOf(),
                isSearchingAddress = false,
                isResolvingCurrentLocation = false,
                isResolvingMapLocation = false,
                addressLookupMessage = if (suggestedCountry == null && suggestion.countryCode != null) {
                    R.string.address_lookup_country_not_supported
                } else {
                    messageRes
                },
            ).withValidation(showRequiredErrors = state.isValidationVisible)
        }
    }

    private fun setMapPickerCoordinateSelection(
        latitude: Double,
        longitude: Double,
        messageRes: Int?,
    ) {
        mapReverseGeocodeJob?.cancel()
        _uiState.update {
            it.copy(
                mapPickerLatitude = latitude,
                mapPickerLongitude = longitude,
                mapPickerSelectedSuggestion = null,
                mapPickerSelectedLocation = SelectedLocationUiModel(
                    title = "${latitude.formatCoordinate()}, ${longitude.formatCoordinate()}",
                    subtitle = "",
                    latitude = latitude,
                    longitude = longitude,
                ),
                addressSuggestions = persistentListOf(),
                isSearchingAddress = false,
                isResolvingCurrentLocation = false,
                isResolvingMapLocation = false,
                addressLookupMessage = messageRes,
            )
        }
    }

    private fun scheduleMapReverseGeocode(latitude: Double, longitude: Double) {
        mapReverseGeocodeJob?.cancel()
        mapReverseGeocodeJob = viewModelScope.launch {
            delay(MAP_REVERSE_GEOCODE_DEBOUNCE_MILLIS)
            val coordinates = AddressCoordinates(latitude = latitude, longitude = longitude)
            _uiState.update { it.copy(isResolvingMapLocation = true, addressLookupMessage = null) }
            reverseGeocodeAddressUseCase(coordinates)
                .onSuccess { suggestion ->
                    val suggestionUiModel = suggestion?.toUiModel()
                    _uiState.update {
                        it.copy(
                            isResolvingMapLocation = false,
                            mapPickerSelectedSuggestion = suggestionUiModel?.withCoordinates(latitude, longitude),
                            mapPickerSelectedLocation = suggestionUiModel?.let { model ->
                                SelectedLocationUiModel(
                                    title = model.title,
                                    subtitle = model.subtitle,
                                    latitude = latitude,
                                    longitude = longitude,
                                )
                            } ?: SelectedLocationUiModel(
                                title = "${latitude.formatCoordinate()}, ${longitude.formatCoordinate()}",
                                subtitle = "",
                                latitude = latitude,
                                longitude = longitude,
                            ),
                            addressLookupMessage = if (suggestionUiModel == null) {
                                R.string.address_location_no_result
                            } else {
                                null
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isResolvingMapLocation = false,
                            mapPickerSelectedSuggestion = null,
                            mapPickerSelectedLocation = SelectedLocationUiModel(
                                title = "${latitude.formatCoordinate()}, ${longitude.formatCoordinate()}",
                                subtitle = "",
                                latitude = latitude,
                                longitude = longitude,
                            ),
                            addressLookupMessage = error.toLookupMessageRes(),
                        )
                    }
                }
        }
    }

    private suspend fun resolveCustomerId(): Long? {
        return when (val accessState = getAuthAccessStateUseCase()) {
            is AuthAccessState.AuthenticatedCustomer -> {
                customerId = accessState.customerId
                accessState.customerId
            }
            AuthAccessState.AuthenticatedMissingCustomerId,
            AuthAccessState.Guest,
                -> {
                customerId = null
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSubmitting = false,
                        isSignInRequired = true,
                        screenError = null,
                    )
                }
                null
            }
        }
    }

    private fun updateField(reducer: AddressFormUiState.() -> AddressFormUiState) {
        _uiState.update { state ->
            state.reducer()
                .copy(screenError = null, addressLookupMessage = null)
                .withValidation(showRequiredErrors = state.isValidationVisible)
        }
    }

    private fun applySuggestion(
        suggestion: AddressSuggestionUiModel,
        messageRes: Int,
        closeMapPicker: Boolean = false,
    ) {
        _uiState.update { state ->
            val suggestedCountry = suggestion.countryCode?.let { state.countries.findCountry(it) }
            val country = suggestedCountry ?: state.selectedCountry ?: FALLBACK_COUNTRY
            val latitude = suggestion.latitude
            val longitude = suggestion.longitude
            state.copy(
                addressSearchQuery = suggestion.title,
                addressSuggestions = persistentListOf(),
                isSearchingAddress = false,
                isResolvingCurrentLocation = false,
                isResolvingMapLocation = false,
                isMapPickerVisible = if (closeMapPicker) false else state.isMapPickerVisible,
                address1 = suggestion.address1,
                city = suggestion.city.orEmpty(),
                province = suggestion.province.orEmpty(),
                country = country.name,
                countryIsoCode = country.isoCode,
                countryDialCode = country.dialCode,
                phone = state.phone.toNationalPhoneInput(country),
                zip = suggestion.postalCode.orEmpty(),
                selectedLocation = if (latitude != null && longitude != null) {
                    SelectedLocationUiModel(
                        title = suggestion.title,
                        subtitle = suggestion.subtitle,
                        latitude = latitude,
                        longitude = longitude,
                    )
                } else {
                    state.selectedLocation
                },
                mapPickerSelectedLocation = null,
                mapPickerSelectedSuggestion = null,
                mapPickerLatitude = latitude ?: state.mapPickerLatitude,
                mapPickerLongitude = longitude ?: state.mapPickerLongitude,
                addressLookupMessage = if (suggestedCountry == null && suggestion.countryCode != null) {
                    R.string.address_lookup_country_not_supported
                } else {
                    messageRes
                },
            ).withValidation(showRequiredErrors = state.isValidationVisible)
        }
    }

    private fun sendEffect(effect: AddressFormUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun AddressSuggestionUiModel.withCoordinates(
        latitude: Double,
        longitude: Double,
    ): AddressSuggestionUiModel =
        copy(latitude = this.latitude ?: latitude, longitude = this.longitude ?: longitude)

    private fun AddressSuggestionUiModel.isAtCoordinates(latitude: Double, longitude: Double): Boolean =
        this.latitude?.distanceFrom(latitude)?.let { latitudeDelta ->
            this.longitude?.distanceFrom(longitude)?.let { longitudeDelta ->
                latitudeDelta <= MAP_PICKER_COORDINATE_SYNC_THRESHOLD &&
                        longitudeDelta <= MAP_PICKER_COORDINATE_SYNC_THRESHOLD
            }
        } == true

    private fun Double.distanceFrom(other: Double): Double = kotlin.math.abs(this - other)

    private fun Double.formatCoordinate(): String =
        String.format(java.util.Locale.US, "%.5f", this)

    private companion object {
        const val MIN_ADDRESS_SEARCH_LENGTH = 3
        const val ADDRESS_SEARCH_DEBOUNCE_MILLIS = 300L
        const val MAP_REVERSE_GEOCODE_DEBOUNCE_MILLIS = 650L
        const val MAP_PICKER_COORDINATE_SYNC_THRESHOLD = 0.0002
        const val DEFAULT_MAP_LATITUDE = 30.0444
        const val DEFAULT_MAP_LONGITUDE = 31.2357
        const val MIN_LATITUDE = -85.0
        const val MAX_LATITUDE = 85.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }
}
