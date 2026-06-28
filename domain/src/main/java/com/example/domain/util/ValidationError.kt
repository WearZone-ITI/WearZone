package com.example.domain.util


sealed interface ValidationError {
    data object BlankName : ValidationError
    data object InvalidEmail : ValidationError
    data object ShortPassword : ValidationError
    data object PasswordMismatch : ValidationError
    data object TermsNotAccepted : ValidationError
}

class ValidationException(
    val error: ValidationError,
) : Exception()
