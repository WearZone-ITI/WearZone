package com.example.wearzone.domain.customer.address.model

data class Country(
    val name: String,
    val isoCode: String,
    val dialCode: String,
    val minNationalNumberLength: Int,
    val maxNationalNumberLength: Int,
    val exampleNationalNumber: String,
    val trunkPrefix: String? = "0",
)
