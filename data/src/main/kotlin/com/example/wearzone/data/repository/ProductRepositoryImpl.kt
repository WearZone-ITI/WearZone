package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.data.remote.datasource.IProductRemoteDataSource
import com.example.wearzone.data.local.home.HomeCacheDao
import com.example.wearzone.data.local.home.HomeCacheEntity
import com.example.wearzone.data.local.home.toCached
import com.example.wearzone.data.local.home.toDomain
import com.example.wearzone.data.local.home.CachedCategory
import com.example.wearzone.data.local.home.CachedBrand
import com.example.wearzone.data.local.home.CachedProduct
import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.model.ProductDetail
import com.example.wearzone.domain.product.repository.IProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: IProductRemoteDataSource,
    private val settingsPreferencesDataSource: ISettingsPreferencesDataSource,
    private val homeCacheDao: HomeCacheDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IProductRepository {

    override suspend fun getCategories(): DataResult<List<Category>> = withContext(ioDispatcher) {
        try {
            val languageCode = currentLanguageCode()
            val dtoList = remoteDataSource.getCategories()
            val domainList = dtoList.map { it.toDomain(languageCode) }
            
            try {
                val cachedList = domainList.map { it.toCached() }
                homeCacheDao.saveCache(
                    HomeCacheEntity(
                        key = "categories_cache",
                        jsonContent = Json.encodeToString(cachedList)
                    )
                )
            } catch (e: Exception) {
                // Ignore cache save errors to prevent breaking app behavior
            }
            
            DataResult.Success(domainList)
        } catch (e: Exception) {
            try {
                val cachedJson = homeCacheDao.getCache("categories_cache")
                if (!cachedJson.isNullOrEmpty()) {
                    val cachedList: List<CachedCategory> = Json.decodeFromString(cachedJson)
                    DataResult.Success(cachedList.map { it.toDomain() })
                } else {
                    DataResult.Error(DomainError.Unknown(e))
                }
            } catch (cacheEx: Exception) {
                DataResult.Error(DomainError.Unknown(e))
            }
        }
    }

    override suspend fun getBrands(): DataResult<List<Brand>> = withContext(ioDispatcher) {
        try {
            val dtoList = remoteDataSource.getBrands()
            val domainList = dtoList.map { it.toDomain() }
            
            try {
                val cachedList = domainList.map { it.toCached() }
                homeCacheDao.saveCache(
                    HomeCacheEntity(
                        key = "brands_cache",
                        jsonContent = Json.encodeToString(cachedList)
                    )
                )
            } catch (e: Exception) {
                // Ignore cache save errors
            }
            
            DataResult.Success(domainList)
        } catch (e: Exception) {
            try {
                val cachedJson = homeCacheDao.getCache("brands_cache")
                if (!cachedJson.isNullOrEmpty()) {
                    val cachedList: List<CachedBrand> = Json.decodeFromString(cachedJson)
                    DataResult.Success(cachedList.map { it.toDomain() })
                } else {
                    DataResult.Error(DomainError.Unknown(e))
                }
            } catch (cacheEx: Exception) {
                DataResult.Error(DomainError.Unknown(e))
            }
        }
    }

    override suspend fun getProducts(
        collectionId: Long?
    ): DataResult<List<Product>> = withContext(ioDispatcher) {

        try {
            val languageCode = currentLanguageCode()
            val dtoList = remoteDataSource.getProducts(collectionId)
            val domainList = dtoList.map { it.toDomain(languageCode) }

            if (collectionId == null) {
                try {
                    val cachedList = domainList.map { it.toCached() }
                    homeCacheDao.saveCache(
                        HomeCacheEntity(
                            key = "products_cache",
                            jsonContent = Json.encodeToString(cachedList)
                        )
                    )
                } catch (e: Exception) {
                    // Ignore cache save errors
                }
            }

            DataResult.Success(domainList)
        } catch (e: Exception) {
            if (collectionId == null) {
                try {
                    val cachedJson = homeCacheDao.getCache("products_cache")
                    if (!cachedJson.isNullOrEmpty()) {
                        val cachedList: List<CachedProduct> = Json.decodeFromString(cachedJson)
                        DataResult.Success(cachedList.map { it.toDomain() })
                    } else {
                        DataResult.Error(DomainError.Unknown(e))
                    }
                } catch (cacheEx: Exception) {
                    DataResult.Error(DomainError.Unknown(e))
                }
            } else {
                DataResult.Error(DomainError.Unknown(e))
            }
        }
    }

    override suspend fun getProductsPreview(limit: Int): DataResult<List<Product>> = withContext(ioDispatcher) {
        try {
            val languageCode = currentLanguageCode()
            val dtoList = remoteDataSource.getProductsPreview(limit)
            DataResult.Success(dtoList.map { it.toDomain(languageCode) })
        } catch (e: Exception) {
            try {
                val cachedJson = homeCacheDao.getCache("products_cache")
                if (!cachedJson.isNullOrEmpty()) {
                    val cachedList: List<CachedProduct> = Json.decodeFromString(cachedJson)
                    DataResult.Success(cachedList.take(limit).map { it.toDomain() })
                } else {
                    DataResult.Error(DomainError.Unknown(e))
                }
            } catch (cacheEx: Exception) {
                DataResult.Error(DomainError.Unknown(e))
            }
        }
    }

    override suspend fun getProductsByVendor(vendor: String): DataResult<List<Product>> = withContext(ioDispatcher) {
        try {
            val languageCode = currentLanguageCode()
            val dtoList = remoteDataSource.getProducts(vendor)
            DataResult.Success(dtoList.map { it.toDomain(languageCode) })
        } catch (e: Exception) {
            DataResult.Error(DomainError.Unknown(e))
        }
    }

    override suspend fun getProductDetail(productId: Long): DataResult<ProductDetail> =
        withContext(ioDispatcher) {
            try {
                val languageCode = currentLanguageCode()
                val shopifyProduct = remoteDataSource.getProductDetail(productId)
                DataResult.Success(
                    shopifyProduct.toDomain(
                        // Mocking real ratings via Random since Shopify API lacks them
                        rating = Random.nextDouble(3.5, 5.0),
                        reviewsCount = Random.nextInt(10, 501),
                        // TODO: Replace with real IWishlistRepository.isInWishlist(productId) call
                        isFavorite = false,
                        languageCode = languageCode,
                    )
                )
            } catch (e: Exception) {
                DataResult.Error(DomainError.Unknown(e))
            }
        }

    private suspend fun currentLanguageCode(): String =
        settingsPreferencesDataSource.observeSettingsPreferences().first().languageCode
}
