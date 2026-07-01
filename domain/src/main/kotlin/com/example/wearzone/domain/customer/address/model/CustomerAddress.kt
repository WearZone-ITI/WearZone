package com.example.wearzone.domain.customer.address.model

data class CustomerAddress(
    val id: Long,
    val customerId: Long,
    val firstName: String?,
    val lastName: String?,
    val company: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val province: String?,
    val country: String?,
    val zip: String?,
    val phone: String?,
    val name: String?,
    val provinceCode: String?,
    val countryCode: String?,
    val countryName: String?,
    val isDefault: Boolean,
)
