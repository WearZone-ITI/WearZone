package com.example.wearzone.presentation.auth.login.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.auth.login.LoginFormState
import com.example.wearzone.presentation.auth.login.LoginUiIntent
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.theme.AppTypography

@Composable
fun LoginForm(
    formState: LoginFormState,
    onIntent: (LoginUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = formState.email,
            onValueChange = { onIntent(LoginUiIntent.OnEmailChanged(it)) },
            label = {
                Text(
                    text = stringResource(R.string.email_address),
                    style = AppTypography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppTheme.colors.surface,
                unfocusedContainerColor = AppTheme.colors.surface,
                focusedIndicatorColor = AppTheme.colors.selected,
                unfocusedIndicatorColor = AppTheme.colors.border,
                focusedTextColor = AppTheme.colors.textPrimary,
                unfocusedTextColor = AppTheme.colors.textPrimary,
            )
        )

        TextField(
            value = formState.password,
            onValueChange = { onIntent(LoginUiIntent.OnPasswordChanged(it)) },
            label = {
                Text(
                    text = stringResource(R.string.password),
                    style = AppTypography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            visualTransformation = if (formState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AppTheme.colors.surface,
                unfocusedContainerColor = AppTheme.colors.surface,
                focusedIndicatorColor = AppTheme.colors.selected,
                unfocusedIndicatorColor = AppTheme.colors.border,
                focusedTextColor = AppTheme.colors.textPrimary,
                unfocusedTextColor = AppTheme.colors.textPrimary,
            ),
            trailingIcon = {
                IconButton(onClick = { onIntent(LoginUiIntent.OnTogglePasswordVisibility) }) {
                    Icon(
                        imageVector = if (formState.isPasswordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,

                        contentDescription = if (formState.isPasswordVisible) stringResource(R.string.hide_password)
                        else stringResource(R.string.show_password),

                        tint = AppTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        )
    }
}
