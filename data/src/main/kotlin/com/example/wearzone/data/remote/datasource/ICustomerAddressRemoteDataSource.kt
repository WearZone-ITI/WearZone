package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.dto.CustomerAddressDto
import com.example.wearzone.data.remote.dto.CustomerAddressRequestDto

interface ICustomerAddressRemoteDataSource {
    suspend fun getAddresses(customerId: Long): List<CustomerAddressDto>
    suspend fun getAddress(customerId: Long, addressId: Long): CustomerAddressDto
    suspend fun createAddress(customerId: Long, request: CustomerAddressRequestDto): CustomerAddressDto
    suspend fun updateAddress(
        customerId: Long,
        addressId: Long,
        request: CustomerAddressRequestDto,
    ): CustomerAddressDto
    suspend fun setDefaultAddress(customerId: Long, addressId: Long): CustomerAddressDto
    suspend fun deleteAddress(customerId: Long, addressId: Long)
}
