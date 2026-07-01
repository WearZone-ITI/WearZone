package com.wearzone.presentation.product.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.presentation.R
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import com.example.wearzone.presentation.product.detail.ProductDetailUiEffect
import com.example.wearzone.presentation.product.detail.ProductDetailUiIntent
import com.example.wearzone.presentation.product.detail.ProductDetailUiState
import com.example.wearzone.presentation.product.detail.ProductDetailViewModel
import io.mockk.coEvery
import io.mockk.mockk
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
class ProductDetailViewModelTest {

    private val getProductDetailUseCase = mockk<GetProductDetailUseCase>()
    private val authRepository = mockk<IAuthRepository>()
    private val savedStateHandle = SavedStateHandle(mapOf("productId" to 1L))

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProductDetailViewModel {
        return ProductDetailViewModel(savedStateHandle, getProductDetailUseCase, authRepository)
    }

    private val dummyProduct = ProductDetail(
        id = 1L,
        title = "Title",
        vendor = "Vendor",
        descriptionHtml = "HTML",
        price = "10",
        images = listOf("img1"),
        availableSizes = listOf("S", "M"),
        rating = 4.8,
        reviewsCount = 120,
        isFavorite = false
    )

    @Test
    fun `init loads product and emits Loading then Success`() = runTest {
        coEvery { getProductDetailUseCase(1L) } returns DataResult.Success(dummyProduct)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // First state is loading
            val loadingState = awaitItem()
            assertTrue(loadingState is ProductDetailUiState.Loading)

            // Second state is success
            val successState = awaitItem()
            assertTrue(successState is ProductDetailUiState.Success)
            assertEquals("Title", (successState as ProductDetailUiState.Success).title)
        }
    }

    @Test
    fun `init loads product and emits Error on failure`() = runTest {
        // الـ ViewModel دايمًا بيبعت نفس resource id ثابت وقت الفشل،
        // مش بيمرر نص الـ exception الحقيقي - فمفيش داعي نغيره هنا
        coEvery { getProductDetailUseCase(1L) } returns DataResult.Error(DomainError.Unknown(Exception("Error message")))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem() is ProductDetailUiState.Loading)

            val errorState = awaitItem()
            assertTrue(errorState is ProductDetailUiState.Error)
            assertEquals(
                R.string.product_detail_error_loading,
                (errorState as ProductDetailUiState.Error).messageRes
            )
        }
    }

    @Test
    fun `SelectSize intent updates selectedSize in Success state`() = runTest {
        coEvery { getProductDetailUseCase(1L) } returns DataResult.Success(dummyProduct)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success

            viewModel.handleIntent(ProductDetailUiIntent.SelectSize("M"))

            val updatedState = awaitItem()
            assertTrue(updatedState is ProductDetailUiState.Success)
            assertEquals("M", (updatedState as ProductDetailUiState.Success).selectedSize)
        }
    }

    @Test
    fun `AddToCart without size emits ShowToast select size first effect`() = runTest {
        coEvery { getProductDetailUseCase(1L) } returns DataResult.Success(dummyProduct)
        coEvery { authRepository.isLoggedIn() } returns true

        val viewModel = createViewModel()

        // Wait for init to finish
        viewModel.uiState.test {
            awaitItem()
            awaitItem() // Now in success state but no size selected
        }

        viewModel.uiEffect.test {
            viewModel.handleIntent(ProductDetailUiIntent.AddToCart)

            val effect = awaitItem()
            assertTrue(effect is ProductDetailUiEffect.ShowToast)
        }
    }

    @Test
    fun `AddToCart by guest emits ShowAuthRequiredError`() = runTest {
        coEvery { getProductDetailUseCase(1L) } returns DataResult.Success(dummyProduct)
        coEvery { authRepository.isLoggedIn() } returns false

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(ProductDetailUiIntent.AddToCart)

            val effect = awaitItem()
            assertTrue(effect is ProductDetailUiEffect.ShowAuthRequiredError)
        }
    }

    @Test
    fun `OnToggleFavorite by guest emits ShowAuthRequiredError`() = runTest {
        coEvery { getProductDetailUseCase(1L) } returns DataResult.Success(dummyProduct)
        coEvery { authRepository.isLoggedIn() } returns false

        val viewModel = createViewModel()

        viewModel.uiEffect.test {
            viewModel.handleIntent(ProductDetailUiIntent.OnToggleFavorite)

            val effect = awaitItem()
            assertTrue(effect is ProductDetailUiEffect.ShowAuthRequiredError)
        }
    }

    @Test
    fun `AddToCart with size by logged-in user emits ShowToast added to cart`() = runTest {
        coEvery { getProductDetailUseCase(1L) } returns DataResult.Success(dummyProduct)
        coEvery { authRepository.isLoggedIn() } returns true

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()
            awaitItem() // Success

            viewModel.handleIntent(ProductDetailUiIntent.SelectSize("M"))
            awaitItem() // Success with size M
        }

        viewModel.uiEffect.test {
            viewModel.handleIntent(ProductDetailUiIntent.AddToCart)

            val effect = awaitItem()
            assertTrue(effect is ProductDetailUiEffect.ShowToast)
        }
    }
}