package com.example.wearzone.domain.checkout.repository

import com.example.wearzone.domain.checkout.model.CheckoutDiscount

interface IDiscountRepository {
    suspend fun validateDiscountCode(code: String): Result<CheckoutDiscount>
}
