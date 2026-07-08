package com.example.wearzone.presentation.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.WearZoneTheme

@Composable
fun SignInRequiredDialog(
    @StringRes messageRes: Int = R.string.sign_in_required_message,
    onSignInRegister: () -> Unit,
    onContinueBrowsing: () -> Unit,
) {
    WearZoneDialog(
        title = stringResource(id = R.string.sign_in_required_title),
        message = stringResource(id = messageRes),
        confirmText = stringResource(R.string.sign_in_register),
        cancelText = stringResource(R.string.continue_browsing),
        onConfirm = onSignInRegister,
        onCancel = onContinueBrowsing,
        onDismiss = onContinueBrowsing,
        icon = Icons.Outlined.Lock,
        iconContentDescription = stringResource(R.string.sign_in_required_lock_content_description),
        tone = WearZoneDialogTone.Info,
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    WearZoneTheme {
        SignInRequiredDialog(
            onSignInRegister = { },
            onContinueBrowsing = { },
        )
    }
}
