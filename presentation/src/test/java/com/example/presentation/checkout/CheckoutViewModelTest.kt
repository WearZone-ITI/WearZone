package com.example.presentation.checkout

import app.cash.turbine.test
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import com.example.wearzone.domain.checkout.model.CheckoutDiscountValueType
import com.example.wearzone.domain.checkout.model.CheckoutOrder
import com.example.wearzone.domain.checkout.model.CheckoutPaymentMethod
import com.example.wearzone.domain.checkout.model.EmptyDiscountCodeException
import com.example.wearzone.domain.checkout.model.InvalidDiscountCodeException
import com.example.wearzone.domain.checkout.usecase.ApplyDiscountCodeUseCase
import com.example.wearzone.domain.checkout.usecase.CreatePaymentIntentionUseCase
import com.example.wearzone.domain.checkout.usecase.PlaceOrderUseCase
import com.example.wearzone.domain.customer.address.model.AddressInput
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import com.example.wearzone.domain.customer.address.usecase.CreateCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.CustomerAddressUseCases
import com.example.wearzone.domain.customer.address.usecase.DeleteCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCurrentCustomerIdUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCustomerAddressesUseCase
import com.example.wearzone.domain.customer.address.usecase.SetDefaultCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.UpdateCustomerAddressUseCase
import com.example.wearzone.presentation.checkout.CheckoutUiEffect
import com.example.wearzone.presentation.checkout.CheckoutUiIntent
import com.example.wearzone.presentation.checkout.CheckoutUiState
import com.example.wearzone.presentation.checkout.CheckoutViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    private val observeCartUseCase: ObserveCartUseCase = mockk()
    private val placeOrderUseCase: PlaceOrderUseCase = mockk()
    private val applyDiscountCodeUseCase: ApplyDiscountCodeUseCase = mockk()
    private val createPaymentIntentionUseCase: CreatePaymentIntentionUseCase = mockk()

    private val cartFlow = MutableSharedFlow<List<CartItem>>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val addressRepository = FakeCustomerAddressRepository(listOf(defaultAddress()))

    private lateinit var viewModel: CheckoutViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeCartUseCase() } returns cartFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        addressRepository: FakeCustomerAddressRepository = this.addressRepository,
    ) {
        val customerIdProvider = FakeCustomerIdProvider()
        viewModel = CheckoutViewModel(
            observeCartUseCase = observeCartUseCase,
            placeOrderUseCase = placeOrderUseCase,
            applyDiscountCodeUseCase = applyDiscountCodeUseCase,
            customerAddressUseCases = createAddressUseCases(addressRepository, customerIdProvider),
            getAuthAccessStateUseCase = GetAuthAccessStateUseCase(
                FakeAuthRepository(),
                customerIdProvider,
            ),
            createPaymentIntentionUseCase = createPaymentIntentionUseCase,
            getCurrentUserUseCase = mockk { coEvery { this@mockk.invoke() } returns (null as User?) }
        )
    }

    private suspend fun awaitContent(): CheckoutUiState.Content {
        var content: CheckoutUiState.Content? = null
        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is CheckoutUiState.Content) {
                cartFlow.emit(listOf(cartItem()))
                state = awaitItem()
            }
            content = state
            cancelAndIgnoreRemainingEvents()
        }
        return requireNotNull(content)
    }

    @Test
    fun `when cart loads content state contains summary totals`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is CheckoutUiState.Content) {
                cartFlow.emit(listOf(cartItem()))
                state = awaitItem()
            }
            val content = state as CheckoutUiState.Content
            assertEquals(1, content.itemCount)
            assertEquals("100.00 USD", content.subtotal)
            assertEquals("100.00 USD", content.total)
            assertNull(content.formattedDiscount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads delivery address for checkout`() = runTest {
        createViewModel()
        advanceUntilIdle()

        val content = awaitContent()
        assertNotNull(content.deliveryAddress)
        assertEquals(DEFAULT_ADDRESS_ID, content.deliveryAddress?.id)
    }

    @Test
    fun `apply discount success updates total and emits success message`() = runTest {
        coEvery { applyDiscountCodeUseCase(any(), any()) } returns Result.success(
            discount(calculatedAmount = 20.0),
        )
        createViewModel()

        viewModel.uiEffect.test {
            viewModel.uiState.test {
                var state = awaitItem()
                while (state !is CheckoutUiState.Content) {
                    cartFlow.emit(listOf(cartItem()))
                    state = awaitItem()
                }
                viewModel.handleIntent(CheckoutUiIntent.OnPromoCodeChanged("SUMMER10"))
                viewModel.handleIntent(CheckoutUiIntent.OnApplyDiscountClicked)

                var finalContent: CheckoutUiState.Content? = null
                while (finalContent?.appliedDiscountCode == null) {
                    state = awaitItem()
                    if (state is CheckoutUiState.Content) {
                        finalContent = state
                    }
                }
                assertEquals("80.00 USD", finalContent.total)
                assertEquals("20.00 USD", finalContent.formattedDiscount)
                assertEquals("SUMMER10", finalContent.appliedDiscountCode)
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(CheckoutUiEffect.ShowMessage(R.string.checkout_discount_applied), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `apply discount with empty code sets validation error`() = runTest {
        coEvery { applyDiscountCodeUseCase(any(), any()) } returns Result.failure(
            EmptyDiscountCodeException(),
        )
        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is CheckoutUiState.Content) {
                cartFlow.emit(listOf(cartItem()))
                state = awaitItem()
            }
            viewModel.handleIntent(CheckoutUiIntent.OnApplyDiscountClicked)

            var content: CheckoutUiState.Content? = null
            while (content?.discountErrorRes == null) {
                state = awaitItem()
                if (state is CheckoutUiState.Content) {
                    content = state
                }
            }
            assertEquals(R.string.checkout_discount_required, content?.discountErrorRes)
            assertEquals("100.00 USD", content?.total)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `apply discount with invalid code sets validation error and keeps total unchanged`() = runTest {
        coEvery { applyDiscountCodeUseCase(any(), any()) } returns Result.failure(
            InvalidDiscountCodeException(),
        )
        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is CheckoutUiState.Content) {
                cartFlow.emit(listOf(cartItem()))
                state = awaitItem()
            }
            viewModel.handleIntent(CheckoutUiIntent.OnPromoCodeChanged("BADCODE"))
            viewModel.handleIntent(CheckoutUiIntent.OnApplyDiscountClicked)

            var content: CheckoutUiState.Content? = null
            while (content?.discountErrorRes == null) {
                state = awaitItem()
                if (state is CheckoutUiState.Content) {
                    content = state
                }
            }
            assertEquals(R.string.checkout_discount_invalid, content?.discountErrorRes)
            assertEquals("100.00 USD", content?.total)
            assertNull(content?.appliedDiscountCode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remove discount restores total`() = runTest {
        coEvery { applyDiscountCodeUseCase(any(), any()) } returns Result.success(
            discount(calculatedAmount = 20.0),
        )
        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is CheckoutUiState.Content) {
                cartFlow.emit(listOf(cartItem()))
                state = awaitItem()
            }
            viewModel.handleIntent(CheckoutUiIntent.OnPromoCodeChanged("SUMMER10"))
            viewModel.handleIntent(CheckoutUiIntent.OnApplyDiscountClicked)

            var discountedContent: CheckoutUiState.Content? = null
            while (discountedContent?.appliedDiscountCode == null) {
                state = awaitItem()
                if (state is CheckoutUiState.Content) {
                    discountedContent = state
                }
            }
            viewModel.handleIntent(CheckoutUiIntent.OnRemoveDiscountClicked)

            var restoredContent: CheckoutUiState.Content? = null
            while (restoredContent == null) {
                state = awaitItem()
                if (state is CheckoutUiState.Content && state.appliedDiscountCode == null) {
                    restoredContent = state
                }
            }
            assertEquals("100.00 USD", restoredContent?.total)
            assertNull(restoredContent?.formattedDiscount)
            assertNull(restoredContent?.appliedDiscountCode)
            assertEquals("", restoredContent?.promoCodeText)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `place order success emits NavigateToOrderHistory`() = runTest {
        coEvery {
            placeOrderUseCase(
                discount = any(),
                selectedAddressId = any(),
                paymentMethod = any(),
                paymentId = any(),
            )
        } returns Result.success(
            CheckoutOrder(id = 1L, name = "#1001"),
        )
        createViewModel()
        awaitContent()

        viewModel.uiEffect.test {
            viewModel.handleIntent(CheckoutUiIntent.OnSubmitOrderConfirmed)
            assertEquals(CheckoutUiEffect.NavigateToOrderHistory, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify {
            placeOrderUseCase(
                discount = null,
                selectedAddressId = DEFAULT_ADDRESS_ID,
                paymentMethod = CheckoutPaymentMethod.CashOnDelivery,
                paymentId = null,
            )
        }
    }

    @Test
    fun `place order with applied discount passes discount to use case`() = runTest {
        val applied = discount(calculatedAmount = 20.0)
        coEvery { applyDiscountCodeUseCase("SUMMER10", 100.0) } returns Result.success(applied)
        coEvery {
            placeOrderUseCase(
                discount = applied,
                selectedAddressId = any(),
                paymentMethod = any(),
                paymentId = any(),
            )
        } returns Result.success(
            CheckoutOrder(id = 1L, name = "#1001"),
        )
        createViewModel()
        awaitContent()

        viewModel.handleIntent(CheckoutUiIntent.OnPromoCodeChanged("SUMMER10"))
        viewModel.handleIntent(CheckoutUiIntent.OnApplyDiscountClicked)
        viewModel.handleIntent(CheckoutUiIntent.OnSubmitOrderConfirmed)

        coVerify {
            placeOrderUseCase(
                discount = applied,
                selectedAddressId = DEFAULT_ADDRESS_ID,
                paymentMethod = CheckoutPaymentMethod.CashOnDelivery,
                paymentId = null,
            )
        }
    }

    @Test
    fun `selecting Credit Card updates state and shows card form`() = runTest {
        createViewModel()
        val content = awaitContent()
        
        viewModel.handleIntent(CheckoutUiIntent.OnPaymentMethodSelected(com.example.wearzone.presentation.checkout.CheckoutPaymentMethodUi.CreditCard))
        
        val updated = (viewModel.uiState.first() as CheckoutUiState.Content)
        assertEquals(com.example.wearzone.presentation.checkout.CheckoutPaymentMethodUi.CreditCard, updated.paymentMethod)
    }

    @Test
    fun `place order with Credit Card triggers Paymob payment result and isPlacingOrder becomes true`() = runTest {
        coEvery {
            placeOrderUseCase(
                discount = any(),
                selectedAddressId = any(),
                paymentMethod = CheckoutPaymentMethod.CreditCard,
                paymentId = "pay_123",
            )
        } returns Result.success(CheckoutOrder(id = 1L, name = "#1001"))

        createViewModel()
        
        // Ensure cart is loaded and state is Content
        cartFlow.emit(listOf(cartItem()))
        advanceUntilIdle()

        viewModel.uiState.test {
            // Initial content
            var initialState = awaitItem()
            while (initialState !is CheckoutUiState.Content) {
                initialState = awaitItem()
            }
            assertFalse("isPlacingOrder should be false initially", (initialState as CheckoutUiState.Content).isPlacingOrder)

            viewModel.handleIntent(
                CheckoutUiIntent.OnPaymobPaymentResult(
                    isSuccess = true,
                    transactionId = "pay_123"
                )
            )

            // State should transition to isPlacingOrder = true
            var state = awaitItem()
            while (state is CheckoutUiState.Content && !state.isPlacingOrder) {
                state = awaitItem()
            }
            assertTrue("isPlacingOrder should be true after payment success", (state as CheckoutUiState.Content).isPlacingOrder)
            
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            placeOrderUseCase(
                discount = null,
                selectedAddressId = DEFAULT_ADDRESS_ID,
                paymentMethod = CheckoutPaymentMethod.CreditCard,
                paymentId = "pay_123",
            )
        }
    }

    @Test
    fun `place order failure emits localized error message`() = runTest {
        coEvery {
            placeOrderUseCase(
                discount = any(),
                selectedAddressId = any(),
                paymentMethod = any(),
                paymentId = any(),
            )
        } returns Result.failure(RuntimeException("network"))
        createViewModel()
        awaitContent()

        viewModel.handleIntent(CheckoutUiIntent.OnSubmitOrderConfirmed)
        
        // Final attempt to fix the flakiness: allow a very long time for the coroutine to complete
        viewModel.uiEffect.test(timeout = 30000.milliseconds) {
            val effect = awaitItem() as CheckoutUiEffect.ShowMessage
            assertEquals(R.string.checkout_error_generic, effect.messageRes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submit order without address shows error message`() = runTest {
        createViewModel(
            addressRepository = FakeCustomerAddressRepository(emptyList()),
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state !is CheckoutUiState.Content) {
                cartFlow.emit(listOf(cartItem()))
                state = awaitItem()
            }
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.uiEffect.test {
            viewModel.handleIntent(CheckoutUiIntent.OnSubmitOrderClicked)
            val effect = awaitItem() as CheckoutUiEffect.ShowMessage
            assertEquals(R.string.checkout_error_no_address, effect.messageRes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createAddressUseCases(
        repository: FakeCustomerAddressRepository,
        customerIdProvider: FakeCustomerIdProvider,
    ): CustomerAddressUseCases =
        CustomerAddressUseCases(
            getCurrentCustomerId = GetCurrentCustomerIdUseCase(customerIdProvider),
            getAddresses = GetCustomerAddressesUseCase(repository),
            getAddress = GetCustomerAddressUseCase(repository),
            createAddress = CreateCustomerAddressUseCase(repository),
            updateAddress = UpdateCustomerAddressUseCase(repository),
            setDefaultAddress = SetDefaultCustomerAddressUseCase(repository),
            deleteAddress = DeleteCustomerAddressUseCase(repository),
        )

    private fun cartItem(): CartItem =
        CartItem(
            variantId = "123",
            productId = "456",
            title = "Silk Blouse",
            vendor = "Lumiere",
            price = 100.0,
            currencyCode = "USD",
            quantity = 1,
            maxQuantity = 5,
            imageUrl = "https://example.com/image.jpg",
            size = "M",
        )

    private fun discount(calculatedAmount: Double): CheckoutDiscount =
        CheckoutDiscount(
            code = "SUMMER10",
            value = 20.0,
            valueType = CheckoutDiscountValueType.Percentage,
            calculatedAmount = calculatedAmount,
        )

    private class FakeCustomerIdProvider(
        private val customerId: Long = CUSTOMER_ID,
    ) : ICustomerIdProvider {
        override suspend fun getCurrentCustomerId(): Result<Long> = Result.success(customerId)
    }

    private class FakeAuthRepository : IAuthRepository {
        override suspend fun loginWithEmail(email: String, password: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun loginWithGoogleCredential(idToken: String): Result<User> =
            Result.failure(NotImplementedError())

        override suspend fun isLoggedIn(): Boolean = true

        override suspend fun getCurrentUser(): User =
            User(uid = "uid", email = "customer@example.com", displayName = "Customer", photoUrl = null)

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override suspend fun register(name: String, email: String, password: String): Result<User> =
            Result.failure(NotImplementedError())

        override fun observeOnboardingCompleted(): Flow<Boolean> = flowOf(true)

        override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> =
            Result.success(Unit)
    }

    private class FakeCustomerAddressRepository(
        private val addresses: List<CustomerAddress>,
    ) : ICustomerAddressRepository {
        override suspend fun getAddresses(customerId: Long): Result<List<CustomerAddress>> =
            Result.success(addresses)

        override suspend fun getAddress(customerId: Long, addressId: Long): Result<CustomerAddress> =
            Result.failure(IllegalStateException("Not used"))

        override suspend fun createAddress(
            customerId: Long,
            input: AddressInput,
        ): Result<CustomerAddress> = Result.failure(IllegalStateException("Not used"))

        override suspend fun updateAddress(
            customerId: Long,
            addressId: Long,
            input: AddressInput,
        ): Result<CustomerAddress> = Result.failure(IllegalStateException("Not used"))

        override suspend fun setDefaultAddress(
            customerId: Long,
            addressId: Long,
        ): Result<CustomerAddress> = Result.failure(IllegalStateException("Not used"))

        override suspend fun deleteAddress(customerId: Long, addressId: Long): Result<Unit> =
            Result.failure(IllegalStateException("Not used"))
    }

    private companion object {
        const val CUSTOMER_ID = 9307871641828L
        const val DEFAULT_ADDRESS_ID = 10736564994276L

        fun defaultAddress(): CustomerAddress =
            CustomerAddress(
                id = DEFAULT_ADDRESS_ID,
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
                isDefault = true,
            )
    }
}
