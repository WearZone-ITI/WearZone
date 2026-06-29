package com.example.wearzone.domain.product.repository

import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Category
import com.example.wearzone.domain.product.model.Product

interface IProductRepository {
    suspend fun getCategories(): DataResult<List<Category>>
    suspend fun getBrands(): DataResult<List<Brand>>
    suspend fun getProducts(): DataResult<List<Product>>
}
