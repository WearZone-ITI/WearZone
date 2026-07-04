package com.example.wearzone.domain.checkout.repository

import com.example.wearzone.domain.checkout.model.PayMockPaymentResponse

interface IPayMockRepository {
    suspend fun processPayment(amount: Double, currency: String): Result<PayMockPaymentResponse>
}
