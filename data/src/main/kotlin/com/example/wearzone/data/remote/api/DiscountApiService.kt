package com.example.wearzone.data.remote.api

import com.example.wearzone.data.remote.dto.DiscountCodeLookupResponseDto
import com.example.wearzone.data.remote.dto.PriceRuleResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscountApiService {
    @GET("admin/api/{version}/discount_codes/lookup.json")
    suspend fun lookupDiscountCode(
        @Path("version") version: String,
        @Query("code") code: String,
    ): DiscountCodeLookupResponseDto

    @GET("admin/api/{version}/price_rules/{priceRuleId}.json")
    suspend fun getPriceRule(
        @Path("version") version: String,
        @Path("priceRuleId") priceRuleId: Long,
    ): PriceRuleResponseDto
}
