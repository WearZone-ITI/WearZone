package com.example.wearzone.presentation.profile.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.presentation.R
import com.example.wearzone.presentation.common.WearZoneDialog
import com.example.wearzone.presentation.common.WearZoneDialogTone

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    WearZoneDialog(
        title = stringResource(R.string.profile_logout_dialog_title),
        message = stringResource(R.string.profile_logout_dialog_message),
        confirmText = stringResource(R.string.profile_logout_dialog_confirm),
        cancelText = stringResource(R.string.profile_logout_dialog_cancel),
        onConfirm = onConfirm,
        onCancel = onDismiss,
        onDismiss = onDismiss,
        icon = Icons.Outlined.ExitToApp,
        iconContentDescription = stringResource(R.string.profile_logout),
        tone = WearZoneDialogTone.Warning,
    )
}
