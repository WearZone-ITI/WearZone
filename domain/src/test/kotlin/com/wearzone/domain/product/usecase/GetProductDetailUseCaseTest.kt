package com.wearzone.domain.product.usecase

import com.wearzone.domain.product.model.Brand
import com.wearzone.domain.product.model.Category
import com.wearzone.domain.product.model.Product
import com.wearzone.domain.product.model.ProductDetail
import com.wearzone.domain.product.repository.IProductRepository
import com.wearzone.domain.common.result.DataResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class FakeProductRepository : IProductRepository {
    var resultToReturn: Result<ProductDetail>? = null
    var shouldThrowCancellationException = false

    override suspend fun getCategories(): DataResult<List<Category>> = TODO("Not yet implemented")
    override suspend fun getBrands(): DataResult<List<Brand>> = TODO("Not yet implemented")
    override suspend fun getProducts(): DataResult<List<Product>> = TODO("Not yet implemented")

    override suspend fun getProductDetail(productId: Long): Result<ProductDetail> {
        if (shouldThrowCancellationException) throw CancellationException("Test cancellation")
        return resultToReturn!!
    }
}

class GetProductDetailUseCaseTest {
    private val fakeRepository = FakeProductRepository()
    private val useCase = GetProductDetailUseCase(fakeRepository)

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val productDetail = ProductDetail(
            id = 1L,
            title = "Test",
            vendor = "Vendor",
            descriptionHtml = "HTML",
            price = "10.00",
            images = emptyList(),
            availableSizes = emptyList(),
            rating = 4.8,
            reviewsCount = 120,
            isFavorite = false
        )
        fakeRepository.resultToReturn = Result.success(productDetail)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(productDetail, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val error = Exception("Network error")
        fakeRepository.resultToReturn = Result.failure(error)

        val result = useCase(1L)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test(expected = CancellationException::class)
    fun `invoke propagates CancellationException`() = runTest {
        fakeRepository.shouldThrowCancellationException = true
        useCase(1L) // Should throw, caught by the expected = CancellationException::class
    }
}
