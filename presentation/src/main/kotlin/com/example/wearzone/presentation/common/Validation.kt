package com.example.wearzone.presentation.common

import android.content.Context
import com.example.wearzone.domain.common.ValidationError
import com.example.presentation.R

fun ValidationError.toMessage(
    context: Context,
): String {
    return when (this) {
        ValidationError.BlankName ->
            context.getString(R.string.error_blank_name)

        ValidationError.InvalidEmail ->
            context.getString(R.string.error_invalid_email)

        ValidationError.ShortPassword ->
            context.getString(R.string.error_short_password)

        ValidationError.PasswordMismatch ->
            context.getString(R.string.error_password_mismatch)

        ValidationError.TermsNotAccepted ->
            context.getString(R.string.error_terms_not_accepted)
    }
}