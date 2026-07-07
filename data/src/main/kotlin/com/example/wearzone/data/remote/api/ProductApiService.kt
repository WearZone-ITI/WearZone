package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.CustomCollectionsResponse
import com.example.wearzone.data.remote.dto.ProductsResponse
import com.example.wearzone.data.remote.dto.SmartCollectionsResponse
import com.example.wearzone.data.remote.dto.ProductDetailDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {
    @GET("admin/api/2024-04/custom_collections.json")
    suspend fun getCustomCollections(
        @Query("limit") limit: Int = 250,
        @Query("fields") fields: String = COLLECTION_FIELDS,
    ): CustomCollectionsResponse

    @GET("admin/api/2024-04/smart_collections.json")
    suspend fun getSmartCollections(
        @Query("limit") limit: Int = 250,
        @Query("fields") fields: String = COLLECTION_FIELDS,
    ): SmartCollectionsResponse

    @GET("admin/api/2024-04/products.json")
    suspend fun getProducts(
        @Query("vendor") vendor: String? = null,
        @Query("collection_id") collectionId: Long? = null,
        @Query("limit") limit: Int = 250,
        @Query("fields") fields: String = PRODUCT_LIST_FIELDS,
    ): ProductsResponse

    @GET("admin/api/2024-04/products.json")
    suspend fun getProductsByIds(
        @Query("ids") ids: String,
        @Query("limit") limit: Int = 250,
        @Query("fields") fields: String = PRODUCT_LIST_FIELDS,
    ): ProductsResponse

    @GET("admin/api/2024-04/products/{product_id}.json")
    suspend fun getProductDetail(
        @Path("product_id") productId: Long,
        @Query("fields") fields: String = PRODUCT_DETAIL_FIELDS,
    ): ProductDetailDto

    companion object {
        const val COLLECTION_FIELDS = "id,title,handle,body_html,image,products_count"
        const val PRODUCT_LIST_FIELDS = "id,title,vendor,product_type,tags,status,variants,images,image"
        const val PRODUCT_DETAIL_FIELDS = "id,title,vendor,body_html,product_type,tags,options,images,image,variants"
    }
}
