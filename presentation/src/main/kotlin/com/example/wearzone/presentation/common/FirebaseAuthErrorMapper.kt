package com.example.wearzone.presentation.common

import androidx.annotation.StringRes
import com.example.presentation.R
import com.example.wearzone.domain.common.FirebaseAuthFailureException

@StringRes
fun Throwable.toFirebaseAuthMessageRes(
    @StringRes fallbackRes: Int = R.string.error_firebase_auth_generic,
): Int {
    val authFailure = this as? FirebaseAuthFailureException ?: return fallbackRes
    return when (authFailure.errorCode) {
        "ERROR_INVALID_EMAIL" -> R.string.error_firebase_invalid_email
        "ERROR_WRONG_PASSWORD" -> R.string.error_firebase_wrong_password
        "ERROR_USER_NOT_FOUND" -> R.string.error_firebase_user_not_found
        "ERROR_EMAIL_ALREADY_IN_USE" -> R.string.error_firebase_email_already_in_use
        "ERROR_WEAK_PASSWORD" -> R.string.error_firebase_weak_password
        "ERROR_NETWORK_REQUEST_FAILED" -> R.string.error_firebase_network_request_failed
        "ERROR_TOO_MANY_REQUESTS" -> R.string.error_firebase_too_many_requests
        else -> fallbackRes
    }
}
