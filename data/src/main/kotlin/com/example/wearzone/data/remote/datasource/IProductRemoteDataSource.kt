package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.dto.BrandDto
import com.example.wearzone.data.remote.dto.CategoryDto
import com.example.wearzone.data.remote.dto.ProductDto
import com.example.wearzone.data.remote.dto.ShopifyProductDetail

interface IProductRemoteDataSource {
    suspend fun getCategories(): List<CategoryDto>
    suspend fun getBrands(): List<BrandDto>
    suspend fun getProductsByIds(productIds: List<Long>): List<ProductDto>
    suspend fun getProducts(vendor: String? = null): List<ProductDto>
    suspend fun getProductDetail(productId: Long): ShopifyProductDetail
}
