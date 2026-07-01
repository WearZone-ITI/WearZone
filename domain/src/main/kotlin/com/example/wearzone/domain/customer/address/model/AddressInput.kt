package com.example.wearzone.domain.customer.address.model

data class AddressInput(
    val firstName: String,
    val lastName: String,
    val company: String?,
    val address1: String,
    val address2: String?,
    val city: String,
    val province: String?,
    val country: String,
    val zip: String,
    val phone: String,
)
