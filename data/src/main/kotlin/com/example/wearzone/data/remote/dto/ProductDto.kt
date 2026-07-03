package com.example.wearzone.data.remote.dto

import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Long,
    val title: String,
    val imageUrl: String? = null,
    val productsCount: Int? = null
) {
    fun toDomain(): Category = Category(id, title, imageUrl, productsCount)
}

@Serializable
data class BrandDto(
    val id: String,
    val title: String,
    val imageUrl: String? = null
) {
    fun toDomain(): Brand = Brand(id, title, imageUrl)
}

@Serializable
data class ProductDto(
    val id: String,
    val variantId: String,
    val title: String,
    val vendor: String,
    val price: Double,
    val currencyCode: String,
    val imageUrl: String? = null
) {
    fun toDomain(): Product = Product(id, variantId, title, vendor, price, currencyCode, imageUrl,isFavorite = false)
}

@Serializable
data class CustomCollectionsResponse(val custom_collections: List<ShopifyCollection> = emptyList())

@Serializable
data class SmartCollectionsResponse(val smart_collections: List<ShopifyCollection> = emptyList())

@Serializable
data class ShopifyCollection(
    val id: Long,
    val title: String,
    val image: ShopifyImage? = null,
) {
    fun toCategoryDto() = CategoryDto(id, title, image?.src)
    fun toBrandDto() = BrandDto(id.toString(), title, image?.src)
}

@Serializable
data class ShopifyImage(val src: String? = null)

@Serializable
data class ProductsResponse(val products: List<ShopifyProduct> = emptyList())

@Serializable
data class ShopifyProduct(
    val id: Long,
    val title: String,
    val vendor: String,
    val variants: List<ShopifyVariant> = emptyList(),
    val image: ShopifyImage? = null
) {
    fun toProductDto() = ProductDto(
        id = id.toString(),
        variantId = variants.firstOrNull()?.id?.toString() ?: id.toString(),
        title = title,
        vendor = vendor,
        price = variants.firstOrNull()?.price?.toDoubleOrNull() ?: 0.0,
        currencyCode = "EGP",
        imageUrl = image?.src
    )
}

@Serializable
data class ShopifyVariant(
    val id: Long? = null,
    val price: String? = null
)
