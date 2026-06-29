package com.wearzone.domain.product.repository

import com.wearzone.domain.common.result.DataResult
import com.wearzone.domain.product.model.Brand
import com.wearzone.domain.product.model.Category
import com.wearzone.domain.product.model.Product
import com.wearzone.domain.product.model.ProductDetail

interface IProductRepository {
    suspend fun getCategories(): DataResult<List<Category>>
    suspend fun getBrands(): DataResult<List<Brand>>
    suspend fun getProducts(): DataResult<List<Product>>
    suspend fun getProductDetail(productId: Long): Result<ProductDetail>
}
