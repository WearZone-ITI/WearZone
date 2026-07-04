package com.example.presentation.cart

import app.cash.turbine.test
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.usecase.ClearCartUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.cart.usecase.RemoveFromCartUseCase
import com.example.wearzone.domain.cart.usecase.UpdateCartQuantityUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.presentation.cart.CartUiEffect
import com.example.wearzone.presentation.cart.CartUiIntent
import com.example.wearzone.presentation.cart.CartUiState
import com.example.wearzone.presentation.cart.CartViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val observeCartUseCase: ObserveCartUseCase = mockk()
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase = mockk()
    private val removeFromCartUseCase: RemoveFromCartUseCase = mockk()
    private val clearCartUseCase: ClearCartUseCase = mockk()
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase = mockk()

    private val cartFlow = MutableSharedFlow<List<CartItem>>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: CartViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = CartViewModel(
            observeCartUseCase,
            updateCartQuantityUseCase,
            removeFromCartUseCase,
            clearCartUseCase,
            getAuthAccessStateUseCase
        )
    }

    // --- Authentication & Initialization Tests ---

    @Test
    fun `init given guest and cart has items maps to Content state`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        val sampleItem = CartItem(
            variantId = "v1", productId = "p1", title = "T-Shirt", vendor = "Nike",
            price = 25.0, quantity = 1, maxQuantity = 5, imageUrl = "url", size = "M", currencyCode = "USD"
        )

        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is CartUiState.Loading || state is CartUiState.Empty) {
                cartFlow.emit(listOf(sampleItem))
                state = awaitItem()
            }
            assertTrue(state is CartUiState.Content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init given cart is empty emits Empty state`() = runTest {
        every { observeCartUseCase() } returns cartFlow

        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is CartUiState.Loading) {
                cartFlow.emit(emptyList())
                state = awaitItem()
            }
            assertEquals(CartUiState.Empty, state)
        }
    }

    @Test
    fun `init given user is authenticated and cart has items maps to Content state properly`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        val sampleItem = CartItem(
            variantId = "v1", productId = "p1", title = "T-Shirt", vendor = "Nike",
            price = 25.0, quantity = 2, maxQuantity = 5, imageUrl = "url", size = "M", currencyCode = "USD"
        )

        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is CartUiState.Loading || state is CartUiState.Empty) {
                cartFlow.emit(listOf(sampleItem))
                state = awaitItem()
            }

            assertTrue(state is CartUiState.Content)
            val content = state as CartUiState.Content
            assertEquals(2, content.itemCount)
            assertEquals("50.00 USD", content.subtotal)
            assertEquals("25.00 USD", content.items.first().price)
            assertEquals(false, content.items.first().isLowStock)
        }
    }

    // --- Quantity Changes ---

    @Test
    fun `OnIncreaseQuantity invokes usecase and respects maxQuantity limit`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        val sampleItem = CartItem(
            variantId = "v1", productId = "p1", title = "T-Shirt", vendor = "Nike",
            price = 20.0, quantity = 4, maxQuantity = 5, imageUrl = "", size = "M", currencyCode = "USD"
        )
        coEvery { updateCartQuantityUseCase("v1", 5) } returns DataResult.Success(Unit)

        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is CartUiState.Loading || state is CartUiState.Empty) {
                cartFlow.emit(listOf(sampleItem))
                state = awaitItem()
            }

            viewModel.handleIntent(CartUiIntent.OnIncreaseQuantity("v1"))
            coVerify(exactly = 1) { updateCartQuantityUseCase("v1", 5) }

            viewModel.handleIntent(CartUiIntent.OnIncreaseQuantity("v1"))
            coVerify(exactly = 2) { updateCartQuantityUseCase("v1", 5) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDecreaseQuantity invokes usecase and respects MIN_QUANTITY limit`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        val sampleItem = CartItem(
            variantId = "v1", productId = "p1", title = "T-Shirt", vendor = "Nike",
            price = 20.0, quantity = 1, maxQuantity = 5, imageUrl = "", size = "M", currencyCode = "USD"
        )

        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is CartUiState.Loading || state is CartUiState.Empty) {
                cartFlow.emit(listOf(sampleItem))
                state = awaitItem()
            }

            viewModel.handleIntent(CartUiIntent.OnDecreaseQuantity("v1"))
            coVerify(exactly = 0) { updateCartQuantityUseCase(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changeQuantity failure emits ShowSnackbar UI effect`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        val sampleItem = CartItem(
            variantId = "v1", productId = "p1", title = "T-Shirt", vendor = "Nike",
            price = 20.0, quantity = 2, maxQuantity = 5, imageUrl = "", size = "M", currencyCode = "USD"
        )

        val mockError = mockk<DomainError>()
        coEvery { updateCartQuantityUseCase("v1", 3) } returns DataResult.Error(mockError)

        createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is CartUiState.Loading || state is CartUiState.Empty) {
                cartFlow.emit(listOf(sampleItem))
                state = awaitItem()
            }

            viewModel.uiEffect.test {
                viewModel.handleIntent(CartUiIntent.OnIncreaseQuantity("v1"))
                assertEquals(CartUiEffect.ShowSnackbar("Unable to update cart. Please try again."), awaitItem())
            }
        }
    }

    // --- Removal & Clearing Flows ---

    @Test
    fun `OnRemoveItemClicked emits ShowRemoveConfirmation effect`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(CartUiIntent.OnRemoveItemClicked("v1"))
            assertEquals(CartUiEffect.ShowRemoveConfirmation("v1"), awaitItem())
        }
    }

    @Test
    fun `OnRemoveItemConfirmed calls usecase successfully`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        coEvery { removeFromCartUseCase("v1") } returns DataResult.Success(Unit)
        createViewModel()

        viewModel.handleIntent(CartUiIntent.OnRemoveItemConfirmed("v1"))

        coVerify(exactly = 1) { removeFromCartUseCase("v1") }
    }

    @Test
    fun `OnClearCartClicked emits ShowClearCartConfirmation effect`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(CartUiIntent.OnClearCartClicked)
            assertEquals(CartUiEffect.ShowClearCartConfirmation, awaitItem())
        }
    }

    @Test
    fun `OnClearCartConfirmed calls usecase successfully`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        coEvery { clearCartUseCase() } returns DataResult.Success(Unit)
        createViewModel()

        viewModel.handleIntent(CartUiIntent.OnClearCartConfirmed)

        coVerify(exactly = 1) { clearCartUseCase() }
    }

    // --- Checkout Flow ---

    @Test
    fun `OnCheckoutClicked navigates to Checkout if authenticated`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        coEvery { getAuthAccessStateUseCase() } returns AuthAccessState.AuthenticatedCustomer(1L)
        createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(CartUiIntent.OnCheckoutClicked)
            assertEquals(CartUiEffect.NavigateToCheckout, awaitItem())
        }
    }

    @Test
    fun `OnCheckoutClicked shows sign in required if guest`() = runTest {
        every { observeCartUseCase() } returns cartFlow
        coEvery { getAuthAccessStateUseCase() } returns AuthAccessState.Guest
        createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(CartUiIntent.OnCheckoutClicked)
            assertEquals(CartUiEffect.ShowSignInRequired, awaitItem())
        }
    }
}
