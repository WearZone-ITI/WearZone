package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ShopifyGraphQlRequest(
    val query: String,
    val variables: ProductLocalizationGraphQlVariables,
)

@Serializable
data class ProductLocalizationGraphQlVariables(
    val ids: List<String>,
    val namespace: String,
)

@Serializable
data class ProductLocalizationGraphQlResponse(
    val data: ProductLocalizationGraphQlData? = null,
    val errors: List<ProductLocalizationGraphQlError> = emptyList(),
)

@Serializable
data class ProductLocalizationGraphQlData(
    val nodes: List<ProductLocalizationGraphQlNode?> = emptyList(),
)

@Serializable
data class ProductLocalizationGraphQlNode(
    @SerialName("legacyResourceId") val legacyResourceId: JsonElement? = null,
    val metafields: ProductLocalizationMetafieldConnection? = null,
)

@Serializable
data class ProductLocalizationMetafieldConnection(
    val nodes: List<ProductLocalizationGraphQlMetafield> = emptyList(),
)

@Serializable
data class ProductLocalizationGraphQlMetafield(
    val namespace: String? = null,
    val key: String,
    val value: String? = null,
)

@Serializable
data class ProductLocalizationGraphQlError(
    val message: String? = null,
)

fun List<ProductLocalizationGraphQlMetafield>.toGraphQlProductLocalizationFields(): ProductLocalizationFields {
    val values = filter { it.namespace == null || it.namespace == "wearzone" }
        .associate { it.key to it.value.orEmpty() }
    return ProductLocalizationFields(
        titleEn = values["title_en"].orEmpty(),
        titleAr = values["title_ar"].orEmpty(),
        descriptionEn = values["description_en"].orEmpty(),
        descriptionAr = values["description_ar"].orEmpty(),
        categoryEn = values["category_en"].orEmpty(),
        categoryAr = values["category_ar"].orEmpty(),
        productTypeEn = values["product_type_en"].orEmpty(),
        productTypeAr = values["product_type_ar"].orEmpty(),
    )
}
