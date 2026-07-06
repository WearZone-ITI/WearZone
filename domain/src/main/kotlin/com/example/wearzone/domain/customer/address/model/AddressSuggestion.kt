package com.example.wearzone.domain.customer.address.model

data class AddressSuggestion(
    val id: String,
    val title: String,
    val subtitle: String,
    val address1: String,
    val city: String?,
    val province: String?,
    val countryName: String?,
    val countryCode: String?,
    val postalCode: String?,
    val coordinates: AddressCoordinates?,
)
