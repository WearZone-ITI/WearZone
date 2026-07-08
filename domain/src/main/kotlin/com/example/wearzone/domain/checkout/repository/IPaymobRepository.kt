package com.example.wearzone.domain.checkout.repository

import com.example.wearzone.domain.checkout.model.CheckoutData
import com.example.wearzone.domain.checkout.model.PaymentIntention

interface IPaymentRepository {
    suspend fun createIntention(checkoutData: CheckoutData): Result<PaymentIntention>
}