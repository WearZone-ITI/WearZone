package com.example.presentation.auth.login.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.presentation.auth.login.LoginFormState
import com.example.presentation.auth.login.LoginUiIntent
import com.example.presentation.common.theme.AppColors
import com.example.presentation.common.theme.AppTypography

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
        androidx.compose.material3.TextField(
            value = formState.email,
            onValueChange = { onIntent(LoginUiIntent.OnEmailChanged(it)) },
            label = { Text("Email Address", style = AppTypography.bodyMedium, color = AppColors.TextPrimary) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = AppColors.InputBackground,
                unfocusedContainerColor = AppColors.InputBackground,
                focusedIndicatorColor = AppColors.TextPrimary,
                unfocusedIndicatorColor = AppColors.InputBorder,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary
            )
        )

        androidx.compose.material3.TextField(
            value = formState.password,
            onValueChange = { onIntent(LoginUiIntent.OnPasswordChanged(it)) },
            label = { Text("Password", style = AppTypography.bodyMedium, color = AppColors.TextPrimary) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            visualTransformation = if (formState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = AppColors.InputBackground,
                unfocusedContainerColor = AppColors.InputBackground,
                focusedIndicatorColor = AppColors.TextPrimary,
                unfocusedIndicatorColor = AppColors.InputBorder,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary
            ),
            trailingIcon = {
                IconButton(onClick = { onIntent(LoginUiIntent.OnTogglePasswordVisibility) }) {
                    Text(if (formState.isPasswordVisible) "Hide" else "Show", style = AppTypography.labelSmall, color = AppColors.TextSecondary)
                }
            }
        )
    }
}
