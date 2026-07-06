package com.example.wearzone.domain.checkout.repository

import com.example.wearzone.domain.checkout.model.CheckoutData
import com.example.wearzone.domain.checkout.model.PaymentIntention

interface IPaymentRepository {
    suspend fun createIntention(checkoutData: CheckoutData): Result<PaymentIntention>
}
data class PaymobBillingData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val apartment: String = "NA",
    val floor: String = "NA",
    val street: String = "NA",
    val building: String = "NA",
    val city: String = "NA",
    val country: String = "NA",
    val state: String = "NA"
)
