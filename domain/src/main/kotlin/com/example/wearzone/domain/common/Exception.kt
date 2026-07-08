package com.example.wearzone.domain.common

sealed class PaymentException(message: String?) : Exception(message) {
    data class ApiError(val code: Int, val errorMessage: String?) : PaymentException(errorMessage)
    data object NetworkError : PaymentException("Network error")
    data class Unknown(val errorMessage: String?) : PaymentException(errorMessage)
}

class FirebaseAuthFailureException(
    val errorCode: String,
) : Exception(errorCode)
