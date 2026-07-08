package com.example.wearzone.presentation.common

import android.content.Context
import androidx.annotation.StringRes
import com.example.presentation.R
import com.example.wearzone.domain.common.ValidationError

@StringRes
fun ValidationError.toMessageRes(): Int {
    return when (this) {
        ValidationError.BlankName -> R.string.error_blank_name
        ValidationError.InvalidEmail -> R.string.error_invalid_email
        ValidationError.ShortPassword -> R.string.error_short_password
        ValidationError.PasswordMismatch -> R.string.error_password_mismatch
        ValidationError.TermsNotAccepted -> R.string.error_terms_not_accepted
    }
}

fun ValidationError.toMessage(
    context: Context,
): String = context.getString(toMessageRes())
