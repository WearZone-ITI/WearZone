package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerAddressesResponseDto(
    val addresses: List<CustomerAddressDto> = emptyList(),
)

@Serializable
data class CustomerAddressResponseDto(
    @SerialName("customer_address")
    val customerAddress: CustomerAddressDto,
)

@Serializable
data class CustomerAddressDto(
    val id: Long,
    @SerialName("customer_id")
    val customerId: Long,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val company: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val zip: String? = null,
    val phone: String? = null,
    val name: String? = null,
    @SerialName("province_code")
    val provinceCode: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null,
    @SerialName("country_name")
    val countryName: String? = null,
    @SerialName("default")
    val isDefault: Boolean = false,
)

@Serializable
data class CustomerAddressRequestDto(
    val address: CustomerAddressInputDto,
)

@Serializable
data class CustomerAddressInputDto(
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val company: String? = null,
    val address1: String,
    val address2: String? = null,
    val city: String,
    val province: String? = null,
    val country: String,
    val zip: String,
    val phone: String,
)

@Serializable
data class ShopifyAddressErrorResponseDto(
    val errors: ShopifyAddressErrorsDto? = null,
)

@Serializable
data class ShopifyAddressErrorsDto(
    val base: List<String> = emptyList(),
)
