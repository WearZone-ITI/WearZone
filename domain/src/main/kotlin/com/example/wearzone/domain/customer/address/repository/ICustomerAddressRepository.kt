package com.example.wearzone.domain.customer.address.repository

import com.example.wearzone.domain.customer.address.model.AddressInput
import com.example.wearzone.domain.customer.address.model.CustomerAddress

interface ICustomerAddressRepository {
    suspend fun getAddresses(customerId: Long): Result<List<CustomerAddress>>
    suspend fun getAddress(customerId: Long, addressId: Long): Result<CustomerAddress>
    suspend fun createAddress(customerId: Long, input: AddressInput): Result<CustomerAddress>
    suspend fun updateAddress(customerId: Long, addressId: Long, input: AddressInput): Result<CustomerAddress>
    suspend fun setDefaultAddress(customerId: Long, addressId: Long): Result<CustomerAddress>
    suspend fun deleteAddress(customerId: Long, addressId: Long): Result<Unit>
}
