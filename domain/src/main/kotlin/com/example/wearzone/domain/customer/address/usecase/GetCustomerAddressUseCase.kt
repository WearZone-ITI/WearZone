package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository

class GetCustomerAddressUseCase(
    private val repository: ICustomerAddressRepository,
) {
    suspend operator fun invoke(customerId: Long, addressId: Long): Result<CustomerAddress> =
        repository.getAddress(customerId, addressId)
}
