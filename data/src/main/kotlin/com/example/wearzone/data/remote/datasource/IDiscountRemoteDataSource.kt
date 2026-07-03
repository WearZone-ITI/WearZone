package com.example.wearzone.data.remote.datasource

import com.example.wearzone.domain.checkout.model.CheckoutDiscount

interface IDiscountRemoteDataSource {
    suspend fun validateDiscountCode(code: String): CheckoutDiscount
}
