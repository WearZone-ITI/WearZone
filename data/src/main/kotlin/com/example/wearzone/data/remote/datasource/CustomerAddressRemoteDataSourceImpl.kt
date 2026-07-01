package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.AddressApiService
import com.example.wearzone.data.remote.dto.CustomerAddressDto
import com.example.wearzone.data.remote.dto.CustomerAddressRequestDto
import com.example.wearzone.data.remote.dto.ShopifyAddressErrorResponseDto
import com.example.wearzone.domain.customer.address.model.CannotDeleteDefaultAddressException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class CustomerAddressRemoteDataSourceImpl @Inject constructor(
    private val apiService: AddressApiService,
    private val json: Json,
) : ICustomerAddressRemoteDataSource {

    override suspend fun getAddresses(customerId: Long): List<CustomerAddressDto> =
        apiService.getAddresses(SHOPIFY_API_VERSION, customerId).addresses

    override suspend fun getAddress(customerId: Long, addressId: Long): CustomerAddressDto =
        apiService.getAddress(SHOPIFY_API_VERSION, customerId, addressId).customerAddress

    override suspend fun createAddress(
        customerId: Long,
        request: CustomerAddressRequestDto,
    ): CustomerAddressDto =
        apiService.createAddress(SHOPIFY_API_VERSION, customerId, request).customerAddress

    override suspend fun updateAddress(
        customerId: Long,
        addressId: Long,
        request: CustomerAddressRequestDto,
    ): CustomerAddressDto =
        apiService.updateAddress(SHOPIFY_API_VERSION, customerId, addressId, request).customerAddress

    override suspend fun setDefaultAddress(customerId: Long, addressId: Long): CustomerAddressDto =
        apiService.setDefaultAddress(SHOPIFY_API_VERSION, customerId, addressId).customerAddress

    override suspend fun deleteAddress(customerId: Long, addressId: Long) {
        val response = apiService.deleteAddress(SHOPIFY_API_VERSION, customerId, addressId)
        if (response.isSuccessful) return

        val errorText = response.errorBody()?.string().orEmpty()
        if (errorText.isDefaultAddressDeleteError()) {
            throw CannotDeleteDefaultAddressException()
        }
        throw IllegalStateException("Unable to delete address.")
    }

    private fun String.isDefaultAddressDeleteError(): Boolean {
        val baseErrors = runCatching {
            json.decodeFromString<ShopifyAddressErrorResponseDto>(this)
                .errors
                ?.base
                .orEmpty()
        }.getOrDefault(emptyList())
        return baseErrors.any { it.contains("default address", ignoreCase = true) }
    }

    private companion object {
        const val SHOPIFY_API_VERSION = "2024-04"
    }
}
