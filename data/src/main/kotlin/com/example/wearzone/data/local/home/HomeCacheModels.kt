package com.example.wearzone.data.local.home

import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class CachedCategory(
    val id: Long,
    val title: String,
    val imageUrl: String?,
    val productsCount: Int?,
    val titleEn: String,
    val titleAr: String
)

fun Category.toCached() = CachedCategory(
    id = id,
    title = title,
    imageUrl = imageUrl,
    productsCount = productsCount,
    titleEn = titleEn,
    titleAr = titleAr
)

fun CachedCategory.toDomain() = Category(
    id = id,
    title = title,
    imageUrl = imageUrl,
    productsCount = productsCount,
    titleEn = titleEn,
    titleAr = titleAr
)

@Serializable
data class CachedBrand(
    val id: String,
    val title: String,
    val imageUrl: String?
)

fun Brand.toCached() = CachedBrand(
    id = id,
    title = title,
    imageUrl = imageUrl
)

fun CachedBrand.toDomain() = Brand(
    id = id,
    title = title,
    imageUrl = imageUrl
)

@Serializable
data class CachedProduct(
    val id: String,
    val variantId: String,
    val title: String,
    val vendor: String,
    val price: Double,
    val currencyCode: String,
    val imageUrl: String?,
    val isFavorite: Boolean,
    val productType: String,
    val tags: List<String>,
    val maxQuantity: Int,
    val isOutOfStock: Boolean,
    val size: String?,
    val color: String?,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val categoryEn: String,
    val categoryAr: String,
    val productTypeEn: String,
    val productTypeAr: String
)

fun Product.toCached() = CachedProduct(
    id = id,
    variantId = variantId,
    title = title,
    vendor = vendor,
    price = price,
    currencyCode = currencyCode,
    imageUrl = imageUrl,
    isFavorite = isFavorite,
    productType = productType,
    tags = tags,
    maxQuantity = maxQuantity,
    isOutOfStock = isOutOfStock,
    size = size,
    color = color,
    titleEn = titleEn,
    titleAr = titleAr,
    descriptionEn = descriptionEn,
    descriptionAr = descriptionAr,
    categoryEn = categoryEn,
    categoryAr = categoryAr,
    productTypeEn = productTypeEn,
    productTypeAr = productTypeAr
)

fun CachedProduct.toDomain() = Product(
    id = id,
    variantId = variantId,
    title = title,
    vendor = vendor,
    price = price,
    currencyCode = currencyCode,
    imageUrl = imageUrl,
    isFavorite = isFavorite,
    productType = productType,
    tags = tags,
    maxQuantity = maxQuantity,
    isOutOfStock = isOutOfStock,
    size = size,
    color = color,
    titleEn = titleEn,
    titleAr = titleAr,
    descriptionEn = descriptionEn,
    descriptionAr = descriptionAr,
    categoryEn = categoryEn,
    categoryAr = categoryAr,
    productTypeEn = productTypeEn,
    productTypeAr = productTypeAr
)
