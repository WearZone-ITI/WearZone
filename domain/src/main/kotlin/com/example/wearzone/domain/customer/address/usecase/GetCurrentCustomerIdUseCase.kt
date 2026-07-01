package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider

class GetCurrentCustomerIdUseCase(
    private val customerIdProvider: ICustomerIdProvider,
) {
    suspend operator fun invoke(): Result<Long> = customerIdProvider.getCurrentCustomerId()
}
