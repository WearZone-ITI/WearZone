package com.wearzone.domain.product.usecase

import com.wearzone.domain.common.result.DataResult
import com.wearzone.domain.product.model.Brand
import com.wearzone.domain.product.model.Category
import com.wearzone.domain.product.model.Product
import com.wearzone.domain.product.repository.IProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: IProductRepository
) {
    suspend fun getCategories(): DataResult<List<Category>> {
        return repository.getCategories()
    }

    suspend fun getBrands(): DataResult<List<Brand>> {
        return repository.getBrands()
    }

    suspend fun getProducts(): DataResult<List<Product>> {
        return repository.getProducts()
    }
}
