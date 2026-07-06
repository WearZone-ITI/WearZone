package com.example.wearzone.domain.checkout.usecase

import com.example.wearzone.domain.checkout.model.CheckoutData
import com.example.wearzone.domain.checkout.model.PaymentIntention
import com.example.wearzone.domain.checkout.repository.IPaymentRepository

class CreatePaymentIntentionUseCase (
    private val repository: IPaymentRepository
) {

    suspend operator fun invoke(
        checkoutData: CheckoutData
    ): Result<PaymentIntention> {
        return repository.createIntention(checkoutData)
    }
}