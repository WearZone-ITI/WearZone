package com.wearzone.data.remote.api

import com.wearzone.data.remote.dto.CustomCollectionsResponse
import com.wearzone.data.remote.dto.ProductsResponse
import com.wearzone.data.remote.dto.SingleProductResponse
import com.wearzone.data.remote.dto.SmartCollectionsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApiService {
    @GET("admin/api/2024-04/custom_collections.json")
    suspend fun getCustomCollections(): CustomCollectionsResponse

    @GET("admin/api/2024-04/smart_collections.json")
    suspend fun getSmartCollections(): SmartCollectionsResponse

    @GET("admin/api/2024-04/products.json")
    suspend fun getProducts(): ProductsResponse

    @GET("admin/api/2024-04/products/{product_id}.json")
    suspend fun getProductDetail(@Path("product_id") productId: Long): SingleProductResponse
}
