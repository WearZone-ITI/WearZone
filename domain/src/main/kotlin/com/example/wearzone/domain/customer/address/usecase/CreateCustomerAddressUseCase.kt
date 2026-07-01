package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.AddressInput
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository

class CreateCustomerAddressUseCase(
    private val repository: ICustomerAddressRepository,
) {
    suspend operator fun invoke(customerId: Long, input: AddressInput): Result<CustomerAddress> =
        repository.createAddress(customerId, input)
}
