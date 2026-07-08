package com.example.wearzone.presentation.wishlist.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.presentation.R
import com.example.wearzone.presentation.common.WearZoneDialog
import com.example.wearzone.presentation.common.WearZoneDialogTone

@Composable
fun RemoveFavoriteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    WearZoneDialog(
        title = stringResource(R.string.wishlist_remove_dialog_title),
        message = stringResource(R.string.wishlist_remove_dialog_subtitle),
        confirmText = stringResource(R.string.wishlist_remove_dialog_confirm),
        cancelText = stringResource(R.string.wishlist_remove_dialog_cancel),
        onConfirm = onConfirm,
        onCancel = onDismiss,
        onDismiss = onDismiss,
        icon = Icons.Outlined.Delete,
        iconContentDescription = stringResource(R.string.wishlist_remove_dialog_confirm),
        tone = WearZoneDialogTone.Destructive,
    )
}
