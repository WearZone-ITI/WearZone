package com.example.wearzone.presentation.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R

@Composable
fun SignInRequiredDialog(
    @StringRes messageRes: Int = R.string.sign_in_required_message,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onContinueBrowsing: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onContinueBrowsing,
        title = { Text(text = stringResource(id = R.string.sign_in_required_title)) },
        text = { Text(text = stringResource(id = messageRes)) },
        confirmButton = {
            TextButton(onClick = onSignIn) {
                Text(text = stringResource(id = R.string.sign_in))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onCreateAccount) {
                    Text(text = stringResource(id = R.string.create_account))
                }
                TextButton(onClick = onContinueBrowsing) {
                    Text(text = stringResource(id = R.string.continue_browsing))
                }
            }
        },
        tonalElevation = 6.dp,
    )
}
