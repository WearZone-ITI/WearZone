package com.example.wearzone.domain.checkout.repository

interface IPaymobRepository {
    suspend fun getPaymentToken(
        amount: Double,
        currency: String,
        billingData: PaymobBillingData
    ): Result<Pair<String, String>>
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
