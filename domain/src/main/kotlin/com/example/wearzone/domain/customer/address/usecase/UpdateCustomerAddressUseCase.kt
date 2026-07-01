package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.AddressInput
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository

class UpdateCustomerAddressUseCase(
    private val repository: ICustomerAddressRepository,
) {
    suspend operator fun invoke(
        customerId: Long,
        addressId: Long,
        input: AddressInput,
    ): Result<CustomerAddress> = repository.updateAddress(customerId, addressId, input)
}
