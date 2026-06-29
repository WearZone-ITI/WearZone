package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.CustomCollectionsResponse
import com.example.wearzone.data.remote.dto.ProductsResponse
import com.example.wearzone.data.remote.dto.SmartCollectionsResponse
import retrofit2.http.GET

interface ProductApiService {
    @GET("admin/api/2024-04/custom_collections.json")
    suspend fun getCustomCollections(): CustomCollectionsResponse

    @GET("admin/api/2024-04/smart_collections.json")
    suspend fun getSmartCollections(): SmartCollectionsResponse

    @GET("admin/api/2024-04/products.json")
    suspend fun getProducts(): ProductsResponse
}
