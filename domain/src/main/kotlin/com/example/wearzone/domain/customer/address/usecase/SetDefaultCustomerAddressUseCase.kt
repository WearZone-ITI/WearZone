package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository

class SetDefaultCustomerAddressUseCase(
    private val repository: ICustomerAddressRepository,
) {
    suspend operator fun invoke(customerId: Long, addressId: Long): Result<CustomerAddress> =
        repository.setDefaultAddress(customerId, addressId)
}
