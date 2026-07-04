package com.example.presentation.product.list

import app.cash.turbine.test
import com.example.presentation.MainDispatcherRule
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.presentation.product.list.ProductListUiEffect
import com.example.wearzone.presentation.product.list.ProductListUiIntent
import com.example.wearzone.presentation.product.list.ProductListViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val getProductsUseCase: GetProductsUseCase = mockk()
    private val observeCartUseCase: ObserveCartUseCase = mockk()

    private lateinit var viewModel: ProductListViewModel

    @Before
    fun setup() {
        io.mockk.every { observeCartUseCase() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        viewModel = ProductListViewModel(getProductsUseCase, observeCartUseCase)
        every { observeCartUseCase() } returns flowOf(emptyList())
    }

    @Test
    fun `loadProducts success updates uiState`() = runTest {

        val products = listOf(
            Product(
                id = "1",
                variantId = "1",
                title = "Nike Air",
                vendor = "Nike",
                price = 100.0,
                currencyCode = "USD",
                imageUrl = ""
            )
        )

        coEvery {
            getProductsUseCase.getProducts(1)
        } returns DataResult.Success(products)

        viewModel.onIntent(ProductListUiIntent.LoadProducts(1))

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(products, state.products)
    }

    @Test
    fun `loadProducts error updates uiState and emits effect`() = runTest {

        val throwable = RuntimeException("No internet")

        val error = DomainError.Network(throwable)

        coEvery {
            getProductsUseCase.getProducts(1)
        } returns DataResult.Error(error)

        viewModel.uiEffect.test {

            viewModel.onIntent(ProductListUiIntent.LoadProducts(1))

            advanceUntilIdle()

            val state = viewModel.uiState.value

            assertFalse(state.isLoading)
            assertEquals(error.toString(), state.error)

            val effect = awaitItem()
            assertTrue(effect is ProductListUiEffect.ShowError)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `product clicked emits navigation effect`() = runTest {

        viewModel.uiEffect.test {

            viewModel.onIntent(
                ProductListUiIntent.ProductClicked(123)
            )

            assertEquals(
                ProductListUiEffect.NavigateToProductDetails(123),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `server error works correctly`() = runTest {

        val error = DomainError.Server(
            code = 500,
            message = "Internal Server Error"
        )

        coEvery {
            getProductsUseCase.getProducts(1)
        } returns DataResult.Error(error)


        viewModel.onIntent(ProductListUiIntent.LoadProducts(1))

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(error.toString(), state.error)
    }
}
