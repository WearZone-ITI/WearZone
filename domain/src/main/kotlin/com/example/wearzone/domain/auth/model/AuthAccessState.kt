package com.example.wearzone.domain.auth.model

sealed interface AuthAccessState {
    data object Guest : AuthAccessState
    data object AuthenticatedMissingCustomerId : AuthAccessState
    data class AuthenticatedCustomer(val customerId: Long) : AuthAccessState
}
