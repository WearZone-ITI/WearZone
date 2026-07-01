package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository

class GetCustomerAddressesUseCase(
    private val repository: ICustomerAddressRepository,
) {
    suspend operator fun invoke(customerId: Long): Result<List<CustomerAddress>> =
        repository.getAddresses(customerId)
}
