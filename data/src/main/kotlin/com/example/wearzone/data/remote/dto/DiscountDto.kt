package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscountCodeLookupResponseDto(
    @SerialName("discount_code")
    val discountCode: DiscountCodeDto,
)

@Serializable
data class DiscountCodeDto(
    val id: Long,
    val code: String,
    @SerialName("price_rule_id")
    val priceRuleId: Long,
)

@Serializable
data class PriceRuleResponseDto(
    @SerialName("price_rule")
    val priceRule: PriceRuleDto,
)

@Serializable
data class PriceRuleDto(
    val id: Long,
    @SerialName("value_type")
    val valueType: String,
    val value: String,
)

@Serializable
data class ShopifyOrderDiscountCodeDto(
    val code: String,
    val amount: String,
    val type: String,
)
