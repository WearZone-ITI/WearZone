package com.example.wearzone.domain.checkout.usecase

import com.example.wearzone.domain.checkout.model.PayMockPaymentResponse
import com.example.wearzone.domain.checkout.repository.IPayMockRepository

class ProcessPayMockPaymentUseCase(
    private val repository: IPayMockRepository,
) {
    suspend operator fun invoke(
        amount: Double,
        currency: String,
    ): Result<PayMockPaymentResponse> {
        return repository.processPayment(amount, currency)
    }
}
