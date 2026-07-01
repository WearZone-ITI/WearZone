package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository

class DeleteCustomerAddressUseCase(
    private val repository: ICustomerAddressRepository,
) {
    suspend operator fun invoke(customerId: Long, addressId: Long): Result<Unit> =
        repository.deleteAddress(customerId, addressId)
}
