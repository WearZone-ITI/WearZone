package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CustomerResponse(
    val customer: ShopifyCustomer
)

@Serializable
data class ShopifyCustomer(
    val id: Long,
    val email: String
)

@Serializable
data class CustomerRequest(
    val customer: CustomerDto
)

@Serializable
data class CustomerDto(
    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    val email: String,

    val phone: String? = null,

    @SerialName("verified_email")
    val verifiedEmail: Boolean = true,

    @SerialName("send_email_welcome")
    val sendEmailWelcome: Boolean = false
)