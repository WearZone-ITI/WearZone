package com.example.presentation.brands

import app.cash.turbine.test
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.usecase.GetBrandsUseCase
import com.example.wearzone.presentation.brands.BrandsUiEffect
import com.example.wearzone.presentation.brands.BrandsUiIntent
import com.example.wearzone.presentation.brands.BrandsUiState
import com.example.wearzone.presentation.brands.BrandsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrandsViewModelTest {

    private val getBrandsUseCase: GetBrandsUseCase = mockk()
    private lateinit var viewModel: BrandsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads brands successfully`() = runTest {
        // Arrange
        val brands = listOf(
            Brand(id = "1", title = "Nike", imageUrl = "url1"),
            Brand(id = "2", title = "Adidas", imageUrl = "url2")
        )
        coEvery { getBrandsUseCase() } returns DataResult.Success(brands)

        // Act
        viewModel = BrandsViewModel(getBrandsUseCase)

        // Assert
        viewModel.uiState.test {
            // First state might be Loading (from initial state before coroutine runs) or we can just await the final state
            val initialState = awaitItem()
            assertTrue(initialState is BrandsUiState.Loading)
            
            val successState = awaitItem()
            assertTrue(successState is BrandsUiState.Success)
            assertEquals(brands.toImmutableList(), (successState as BrandsUiState.Success).brands)
        }
    }

    @Test
    fun `init loads brands with error`() = runTest {
        // Arrange
        val errorMessage = "Server error"
        coEvery { getBrandsUseCase() } returns DataResult.Error(DomainError.Server(500, errorMessage))

        // Act
        viewModel = BrandsViewModel(getBrandsUseCase)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState is BrandsUiState.Loading)
            
            val errorState = awaitItem()
            assertTrue(errorState is BrandsUiState.Error)
            assertEquals(errorMessage, (errorState as BrandsUiState.Error).message)
        }
    }

    @Test
    fun `OnBrandClicked intent sends NavigateToVendorProducts effect`() = runTest {
        // Arrange
        coEvery { getBrandsUseCase() } returns DataResult.Success(emptyList())
        viewModel = BrandsViewModel(getBrandsUseCase)
        
        // Let the init load complete
        testScheduler.advanceUntilIdle()

        // Act & Assert
        viewModel.uiEffect.test {
            viewModel.handleIntent(BrandsUiIntent.OnBrandClicked("Nike"))
            val effect = awaitItem()
            assertTrue(effect is BrandsUiEffect.NavigateToVendorProducts)
            assertEquals("Nike", (effect as BrandsUiEffect.NavigateToVendorProducts).vendorName)
        }
    }
}
