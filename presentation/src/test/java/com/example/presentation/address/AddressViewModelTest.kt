package com.example.presentation.address

import app.cash.turbine.test
import com.example.presentation.MainDispatcherRule
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.customer.address.model.AddressInput
import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.model.AddressSuggestion
import com.example.wearzone.domain.customer.address.model.Country
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.IAddressLookupRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository
import com.example.wearzone.domain.customer.address.repository.ICountryRepository
import com.example.wearzone.domain.customer.address.repository.ICurrentLocationRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import com.example.wearzone.domain.customer.address.usecase.CreateCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.CustomerAddressUseCases
import com.example.wearzone.domain.customer.address.usecase.DeleteCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCountriesUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCurrentAddressCoordinatesUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCurrentCustomerIdUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCustomerAddressesUseCase
import com.example.wearzone.domain.customer.address.usecase.ReverseGeocodeAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.SearchAddressSuggestionsUseCase
import com.example.wearzone.domain.customer.address.usecase.SetDefaultCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.UpdateCustomerAddressUseCase
import com.example.wearzone.presentation.address.form.AddressFormUiIntent
import com.example.wearzone.presentation.address.form.AddressFormViewModel
import com.example.wearzone.presentation.address.list.AddressListUiEffect
import com.example.wearzone.presentation.address.list.AddressListUiIntent
import com.example.wearzone.presentation.address.list.AddressListUiState
import com.example.wearzone.presentation.address.list.AddressListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddressViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `address list loads with resolved Shopify customer id`() = runTest {
        val repository = FakeCustomerAddressRepository(
            addresses = listOf(address()),
        )
        val viewModel = createAddressListViewModel(repository)
        advanceUntilIdle()

        assertEquals(CUSTOMER_ID, repository.lastGetAddressesCustomerId)
        assertTrue(viewModel.uiState.value is AddressListUiState.Content)
    }

    @Test
    fun `delete click emits confirmation before repository delete`() = runTest {
        val repository = FakeCustomerAddressRepository(
            addresses = listOf(address(isDefault = true)),
        )
        val viewModel = createAddressListViewModel(repository)
        advanceUntilIdle()
        val content = viewModel.uiState.value as AddressListUiState.Content

        viewModel.uiEffect.test {
            viewModel.handleIntent(AddressListUiIntent.OnDeleteClicked(content.addresses.first()))

            val effect = awaitItem()
            assertTrue(effect is AddressListUiEffect.ShowDeleteConfirmation)
            assertEquals(0, repository.deleteCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `missing street address prevents form save`() = runTest {
        val repository = FakeCustomerAddressRepository()
        val viewModel = createAddressFormViewModel(repository)

        viewModel.handleIntent(AddressFormUiIntent.OnInitialize(null))
        viewModel.handleIntent(AddressFormUiIntent.OnRecipientNameChanged("Mobile Customer"))
        viewModel.handleIntent(AddressFormUiIntent.OnPhoneChanged("01012345678"))
        viewModel.handleIntent(AddressFormUiIntent.OnCityChanged("Cairo"))
        viewModel.handleIntent(AddressFormUiIntent.OnSaveClicked)

        assertEquals(R.string.address_error_required_field, viewModel.uiState.value.address1Error)
        assertEquals(0, repository.createCalls)
    }

    @Test
    fun `invalid phone prevents form save`() = runTest {
        val repository = FakeCustomerAddressRepository()
        val viewModel = createAddressFormViewModel(repository)

        fillValidAddress(viewModel)
        viewModel.handleIntent(AddressFormUiIntent.OnPhoneChanged("12345"))
        viewModel.handleIntent(AddressFormUiIntent.OnSaveClicked)

        assertEquals(R.string.address_error_invalid_phone, viewModel.uiState.value.phoneError)
        assertEquals(0, repository.createCalls)
    }

    @Test
    fun `postal code with letters prevents form save`() = runTest {
        val repository = FakeCustomerAddressRepository()
        val viewModel = createAddressFormViewModel(repository)

        fillValidAddress(viewModel)
        viewModel.handleIntent(AddressFormUiIntent.OnZipChanged("11A11"))
        viewModel.handleIntent(AddressFormUiIntent.OnSaveClicked)

        assertEquals(R.string.address_error_invalid_postal_code, viewModel.uiState.value.zipError)
        assertEquals(0, repository.createCalls)
    }

    @Test
    fun `set as default after create calls set default use case`() = runTest {
        val repository = FakeCustomerAddressRepository()
        val viewModel = createAddressFormViewModel(repository)

        fillValidAddress(viewModel)
        viewModel.handleIntent(AddressFormUiIntent.OnDefaultChanged(true))
        viewModel.handleIntent(AddressFormUiIntent.OnSaveClicked)
        advanceUntilIdle()

        assertEquals(1, repository.createCalls)
        assertEquals(1, repository.setDefaultCalls)
        assertEquals(CUSTOMER_ID, repository.lastCreateCustomerId)
        assertEquals(CUSTOMER_ID, repository.lastSetDefaultCustomerId)
        assertEquals("+201012345678", repository.lastCreateInput?.phone)
    }

    @Test
    fun `selecting another country changes phone normalization`() = runTest {
        val repository = FakeCustomerAddressRepository()
        val viewModel = createAddressFormViewModel(repository)

        fillValidAddress(viewModel)
        viewModel.handleIntent(AddressFormUiIntent.OnCountrySelected("SA"))
        viewModel.handleIntent(AddressFormUiIntent.OnPhoneChanged("+966512345678"))
        viewModel.handleIntent(AddressFormUiIntent.OnSaveClicked)
        advanceUntilIdle()

        assertEquals("+966512345678", repository.lastCreateInput?.phone)
        assertEquals("Saudi Arabia", repository.lastCreateInput?.country)
    }

    @Test
    fun `selecting address suggestion fills address fields and country`() = runTest {
        val lookupRepository = FakeAddressLookupRepository(
            suggestions = listOf(
                AddressSuggestion(
                    id = "mapbox.1",
                    title = "King Fahd Road",
                    subtitle = "King Fahd Road, Riyadh, Saudi Arabia",
                    address1 = "King Fahd Road",
                    city = "Riyadh",
                    province = "Riyadh Province",
                    countryName = "Saudi Arabia",
                    countryCode = "SA",
                    postalCode = "12271",
                    coordinates = AddressCoordinates(latitude = 24.7136, longitude = 46.6753),
                )
            ),
        )
        val viewModel = createAddressFormViewModel(
            repository = FakeCustomerAddressRepository(),
            lookupRepository = lookupRepository,
        )

        viewModel.handleIntent(AddressFormUiIntent.OnInitialize(null))
        advanceUntilIdle()
        viewModel.handleIntent(AddressFormUiIntent.OnAddressSearchQueryChanged("King Fahd"))
        advanceUntilIdle()
        viewModel.handleIntent(AddressFormUiIntent.OnAddressSuggestionSelected("mapbox.1"))

        val state = viewModel.uiState.value
        assertEquals("King Fahd Road", state.address1)
        assertEquals("Riyadh", state.city)
        assertEquals("SA", state.countryIsoCode)
        assertEquals("+966", state.countryDialCode)
    }

    @Test
    fun `map picker suggestion fills form only after confirm`() = runTest {
        val lookupRepository = FakeAddressLookupRepository(
            suggestions = listOf(
                AddressSuggestion(
                    id = "mapbox.2",
                    title = "Tahlia Street",
                    subtitle = "Tahlia Street, Riyadh, Saudi Arabia",
                    address1 = "Tahlia Street",
                    city = "Riyadh",
                    province = "Riyadh Province",
                    countryName = "Saudi Arabia",
                    countryCode = "SA",
                    postalCode = "12241",
                    coordinates = AddressCoordinates(latitude = 24.7000, longitude = 46.6800),
                )
            ),
        )
        val viewModel = createAddressFormViewModel(
            repository = FakeCustomerAddressRepository(),
            lookupRepository = lookupRepository,
        )

        viewModel.handleIntent(AddressFormUiIntent.OnInitialize(null))
        advanceUntilIdle()
        viewModel.handleIntent(AddressFormUiIntent.OnPickOnMapClicked)
        viewModel.handleIntent(AddressFormUiIntent.OnAddressSearchQueryChanged("Tahlia"))
        advanceUntilIdle()
        viewModel.handleIntent(AddressFormUiIntent.OnAddressSuggestionSelected("mapbox.2"))

        assertEquals("", viewModel.uiState.value.address1)
        assertEquals("Tahlia Street", viewModel.uiState.value.mapPickerSelectedLocation?.title)

        viewModel.handleIntent(AddressFormUiIntent.OnConfirmMapLocationClicked)

        val state = viewModel.uiState.value
        assertEquals("Tahlia Street", state.address1)
        assertEquals("Riyadh", state.city)
        assertEquals("SA", state.countryIsoCode)
    }

    @Test
    fun `guest address list shows sign in required and does not load addresses`() = runTest {
        val repository = FakeCustomerAddressRepository(addresses = listOf(address()))
        val viewModel = createAddressListViewModel(repository, user = null)
        advanceUntilIdle()

        assertEquals(AddressListUiState.SignInRequired, viewModel.uiState.value)
        assertEquals(null, repository.lastGetAddressesCustomerId)
    }

    private fun TestScope.fillValidAddress(viewModel: AddressFormViewModel) {
        viewModel.handleIntent(AddressFormUiIntent.OnInitialize(null))
        advanceUntilIdle()
        viewModel.handleIntent(AddressFormUiIntent.OnRecipientNameChanged("Mobile Customer"))
        viewModel.handleIntent(AddressFormUiIntent.OnPhoneChanged("01012345678"))
        viewModel.handleIntent(AddressFormUiIntent.OnAddress1Changed("456 Mobile App Avenue"))
        viewModel.handleIntent(AddressFormUiIntent.OnCityChanged("Cairo"))
        viewModel.handleIntent(AddressFormUiIntent.OnProvinceChanged("Cairo Governorate"))
        viewModel.handleIntent(AddressFormUiIntent.OnZipChanged("11511"))
    }

    private fun createUseCases(
        repository: FakeCustomerAddressRepository = FakeCustomerAddressRepository(),
        provider: ICustomerIdProvider = FakeCustomerIdProvider(),
    ): CustomerAddressUseCases =
        CustomerAddressUseCases(
            getCurrentCustomerId = GetCurrentCustomerIdUseCase(provider),
            getAddresses = GetCustomerAddressesUseCase(repository),
            getAddress = GetCustomerAddressUseCase(repository),
            createAddress = CreateCustomerAddressUseCase(repository),
            updateAddress = UpdateCustomerAddressUseCase(repository),
            setDefaultAddress = SetDefaultCustomerAddressUseCase(repository),
            deleteAddress = DeleteCustomerAddressUseCase(repository),
        )

    private fun createAddressListViewModel(
        repository: FakeCustomerAddressRepository = FakeCustomerAddressRepository(),
        provider: ICustomerIdProvider = FakeCustomerIdProvider(),
        user: User? = USER,
    ): AddressListViewModel =
        AddressListViewModel(
            addressUseCases = createUseCases(repository, provider),
            getAuthAccessStateUseCase = createAuthAccessUseCase(provider, user),
        )

    private fun createAddressFormViewModel(
        repository: FakeCustomerAddressRepository = FakeCustomerAddressRepository(),
        provider: ICustomerIdProvider = FakeCustomerIdProvider(),
        user: User? = USER,
        countryRepository: ICountryRepository = FakeCountryRepository(),
        lookupRepository: IAddressLookupRepository = FakeAddressLookupRepository(),
        locationRepository: ICurrentLocationRepository = FakeCurrentLocationRepository(),
    ): AddressFormViewModel =
        AddressFormViewModel(
            addressUseCases = createUseCases(repository, provider),
            getAuthAccessStateUseCase = createAuthAccessUseCase(provider, user),
            getCountriesUseCase = GetCountriesUseCase(countryRepository),
            searchAddressSuggestionsUseCase = SearchAddressSuggestionsUseCase(lookupRepository),
            reverseGeocodeAddressUseCase = ReverseGeocodeAddressUseCase(lookupRepository),
            getCurrentAddressCoordinatesUseCase = GetCurrentAddressCoordinatesUseCase(locationRepository),
        )

    private fun createAuthAccessUseCase(
        provider: ICustomerIdProvider,
        user: User?,
    ): GetAuthAccessStateUseCase =
        GetAuthAccessStateUseCase(
            authRepository = FakeAuthRepository(user),
            customerIdProvider = provider,
        )

    private class FakeCustomerIdProvider(
        private val customerId: Long = CUSTOMER_ID,
    ) : ICustomerIdProvider {
        override suspend fun getCurrentCustomerId(): Result<Long> = Result.success(customerId)
    }

    private class FakeAuthRepository(
        private val currentUser: User?,
    ) : IAuthRepository {
        override suspend fun loginWithEmail(email: String, password: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override suspend fun loginWithGoogleCredential(idToken: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override suspend fun isLoggedIn(): Boolean = currentUser != null

        override suspend fun getCurrentUser(): User? = currentUser

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun register(name: String, email: String, password: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override fun observeOnboardingCompleted(): kotlinx.coroutines.flow.Flow<Boolean> =
            kotlinx.coroutines.flow.flowOf(false)

        override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> =
            Result.success(Unit)
    }

    private class FakeCustomerAddressRepository(
        private var addresses: List<CustomerAddress> = emptyList(),
    ) : ICustomerAddressRepository {
        var createCalls = 0
            private set
        var deleteCalls = 0
            private set
        var setDefaultCalls = 0
            private set
        var lastGetAddressesCustomerId: Long? = null
            private set
        var lastCreateCustomerId: Long? = null
            private set
        var lastSetDefaultCustomerId: Long? = null
            private set
        var lastCreateInput: AddressInput? = null
            private set

        override suspend fun getAddresses(customerId: Long): Result<List<CustomerAddress>> {
            lastGetAddressesCustomerId = customerId
            return Result.success(addresses)
        }

        override suspend fun getAddress(customerId: Long, addressId: Long): Result<CustomerAddress> =
            Result.success(address(id = addressId))

        override suspend fun createAddress(
            customerId: Long,
            input: AddressInput,
        ): Result<CustomerAddress> {
            createCalls += 1
            lastCreateCustomerId = customerId
            lastCreateInput = input
            return Result.success(address(id = CREATED_ADDRESS_ID))
        }

        override suspend fun updateAddress(
            customerId: Long,
            addressId: Long,
            input: AddressInput,
        ): Result<CustomerAddress> = Result.success(address(id = addressId))

        override suspend fun setDefaultAddress(
            customerId: Long,
            addressId: Long,
        ): Result<CustomerAddress> {
            setDefaultCalls += 1
            lastSetDefaultCustomerId = customerId
            return Result.success(address(id = addressId, isDefault = true))
        }

        override suspend fun deleteAddress(customerId: Long, addressId: Long): Result<Unit> {
            deleteCalls += 1
            return Result.success(Unit)
        }
    }

    private class FakeCountryRepository : ICountryRepository {
        override suspend fun getCountries(): Result<List<Country>> =
            Result.success(
                listOf(
                    Country("Egypt", "EG", "+20", 10, 10, "1012345678"),
                    Country("Saudi Arabia", "SA", "+966", 9, 9, "512345678"),
                )
            )
    }

    private class FakeAddressLookupRepository(
        private val suggestions: List<AddressSuggestion> = emptyList(),
        private val reverseSuggestion: AddressSuggestion? = suggestions.firstOrNull(),
    ) : IAddressLookupRepository {
        override suspend fun searchAddressSuggestions(
            query: String,
            countryIsoCode: String?,
        ): Result<List<AddressSuggestion>> = Result.success(suggestions)

        override suspend fun reverseGeocode(
            coordinates: AddressCoordinates,
        ): Result<AddressSuggestion?> = Result.success(reverseSuggestion)
    }

    private class FakeCurrentLocationRepository : ICurrentLocationRepository {
        override suspend fun getCurrentCoordinates(): Result<AddressCoordinates> =
            Result.success(AddressCoordinates(latitude = 30.0444, longitude = 31.2357))
    }

    private companion object {
        const val CUSTOMER_ID = 9307871641828L
        const val CREATED_ADDRESS_ID = 10757933433060L
        val USER = User(
            uid = "firebase-user",
            email = "user@example.com",
            displayName = "Mobile Customer",
            photoUrl = null,
        )

        fun address(
            id: Long = 10736564994276L,
            isDefault: Boolean = false,
        ): CustomerAddress =
            CustomerAddress(
                id = id,
                customerId = CUSTOMER_ID,
                firstName = "Mobile",
                lastName = "Customer",
                company = null,
                address1 = "456 Mobile App Avenue",
                address2 = null,
                city = "Cairo",
                province = "Cairo",
                country = "Egypt",
                zip = "11511",
                phone = "+200000000000",
                name = "Mobile Customer",
                provinceCode = "C",
                countryCode = "EG",
                countryName = "Egypt",
                isDefault = isDefault,
            )
    }
}
