package com.example.wearzone.domain.product.usecase

import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Category
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.domain.product.repository.IProductRepository

class GetProductsUseCase(
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
