package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.datasource.IProductRemoteDataSource
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.common.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: IProductRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IProductRepository {

    override suspend fun getCategories(): DataResult<List<Category>> = withContext(ioDispatcher) {
        try {
            val dtoList = remoteDataSource.getCategories()
            DataResult.Success(dtoList.map { it.toDomain() })
        } catch (e: Exception) {
            DataResult.Error(DomainError.Unknown(e))
        }
    }

    override suspend fun getBrands(): DataResult<List<Brand>> = withContext(ioDispatcher) {
        try {
            val dtoList = remoteDataSource.getBrands()
            DataResult.Success(dtoList.map { it.toDomain() })
        } catch (e: Exception) {
            DataResult.Error(DomainError.Unknown(e))
        }
    }

    override suspend fun getProducts(
        collectionId: Long?
    ): DataResult<List<Product>> = withContext(ioDispatcher) {

        try {

            val dtoList = remoteDataSource.getProducts(collectionId)

            DataResult.Success(
                dtoList.map { it.toDomain() }
            )

        } catch (e: Exception) {

            DataResult.Error(DomainError.Unknown(e))

        }
    }

    override suspend fun getProductDetail(productId: Long): DataResult<ProductDetail> =
        withContext(ioDispatcher) {
            try {
                val shopifyProduct = remoteDataSource.getProductDetail(productId)
                DataResult.Success(
                    shopifyProduct.toDomain(
                        // Mocking real ratings via Random since Shopify API lacks them
                        rating = Random.nextDouble(3.5, 5.0),
                        reviewsCount = Random.nextInt(10, 501),
                        // TODO: Replace with real IWishlistRepository.isInWishlist(productId) call
                        isFavorite = false,
                    )
                )
            } catch (e: Exception) {
                DataResult.Error(DomainError.Unknown(e))
            }
        }
}
