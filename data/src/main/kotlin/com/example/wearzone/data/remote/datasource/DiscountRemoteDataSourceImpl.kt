package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.DiscountApiService
import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import com.example.wearzone.domain.checkout.model.CheckoutDiscountValueType
import javax.inject.Inject
import kotlin.math.abs

class DiscountRemoteDataSourceImpl @Inject constructor(
    private val apiService: DiscountApiService,
) : IDiscountRemoteDataSource {

    //Discount code example : BUY4GET25

    override suspend fun validateDiscountCode(code: String): CheckoutDiscount {
        val lookupResponse = apiService.lookupDiscountCode(
            version = SHOPIFY_API_VERSION,
            code = code,
        )
        val priceRule = apiService.getPriceRule(
            version = SHOPIFY_API_VERSION,
            priceRuleId = lookupResponse.discountCode.priceRuleId,
        ).priceRule

        return CheckoutDiscount(
            code = lookupResponse.discountCode.code,
            value = abs(priceRule.value.toDoubleOrNull() ?: 0.0),
            valueType = priceRule.valueType.toDiscountValueType(),
            calculatedAmount = PLACEHOLDER_CALCULATED_AMOUNT,
        )
    }

    private fun String.toDiscountValueType(): CheckoutDiscountValueType =
        when (lowercase()) {
            VALUE_TYPE_PERCENTAGE -> CheckoutDiscountValueType.Percentage
            VALUE_TYPE_FIXED_AMOUNT -> CheckoutDiscountValueType.FixedAmount
            else -> CheckoutDiscountValueType.FixedAmount
        }

    private companion object {
        const val SHOPIFY_API_VERSION = "2024-04"
        const val VALUE_TYPE_PERCENTAGE = "percentage"
        const val VALUE_TYPE_FIXED_AMOUNT = "fixed_amount"
        const val PLACEHOLDER_CALCULATED_AMOUNT = 0.0
    }
}
