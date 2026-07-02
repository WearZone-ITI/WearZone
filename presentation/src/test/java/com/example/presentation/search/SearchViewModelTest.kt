package com.example.presentation.search

import com.example.presentation.MainDispatcherRule
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.product.usecase.SearchProductsUseCase
import com.example.wearzone.domain.search.usecase.ClearRecentSearchesUseCase
import com.example.wearzone.domain.search.usecase.GetRecentSearchesUseCase
import com.example.wearzone.domain.search.usecase.SaveRecentSearchUseCase
import com.example.wearzone.presentation.search.SearchUiIntent
import com.example.wearzone.presentation.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchProductsUseCase: SearchProductsUseCase = mockk()
    private val getProductsUseCase: GetProductsUseCase = mockk()
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase = mockk()
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase = mockk()
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase = mockk()
    private val observeCartUseCase: ObserveCartUseCase = mockk()

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        every { getRecentSearchesUseCase() } returns flowOf(emptyList())
        every { observeCartUseCase() } returns flowOf(emptyList())
        coEvery { getProductsUseCase.getBrands() } returns DataResult.Success(emptyList())
        coEvery { getProductsUseCase.getCategories() } returns DataResult.Success(emptyList())
        coEvery { searchProductsUseCase(any()) } returns DataResult.Success(emptyList())
    }

    private fun createViewModel() {
        viewModel = SearchViewModel(
            searchProductsUseCase,
            getProductsUseCase,
            getRecentSearchesUseCase,
            saveRecentSearchUseCase,
            clearRecentSearchesUseCase,
            observeCartUseCase
        )
    }

    @Test
    fun init_shouldLoadFiltersAndSearchProductsSuccessfully() = runTest {
        coEvery { getProductsUseCase.getBrands() } returns DataResult.Success(emptyList())
        coEvery { getProductsUseCase.getCategories() } returns DataResult.Success(emptyList())
        coEvery { searchProductsUseCase(any()) } returns DataResult.Success(emptyList())

        createViewModel()

        val currentState = viewModel.uiState.value
        assertEquals(false, currentState.isLoading)
        assertTrue(currentState.products.isEmpty())
    }

    @Test
    fun onQueryChanged_shouldUpdateQueryInStateAndTriggerDebouncedSearch() = runTest {
        createViewModel()
        val expectedQuery = "Kotlin"
        coEvery { searchProductsUseCase(any()) } returns DataResult.Success(emptyList())

        viewModel.handleIntent(SearchUiIntent.OnQueryChanged(expectedQuery))

        assertEquals(expectedQuery, viewModel.uiState.value.query)

        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(301)

        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun searchProducts_withErrorResult_shouldUpdateStateWithErrorMessage() = runTest {
        val errorResult = DataResult.Error(DomainError.Network(exception = Exception("Network error")))
        coEvery { searchProductsUseCase(any()) } returns errorResult
        createViewModel()

        val currentState = viewModel.uiState.value
        assertEquals(false, currentState.isLoading)
        assertEquals(true, currentState.hasError)
        assertEquals("search_error_network", currentState.errorMessage)
    }

    @Test
    fun `cart item count is observed and updated in state`() = runTest {
        createViewModel()
        val currentCartCount = viewModel.uiState.value.cartItemCount
        assertEquals(0, currentCartCount)
    }
}