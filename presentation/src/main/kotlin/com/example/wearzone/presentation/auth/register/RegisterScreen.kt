package com.example.wearzone.presentation.auth.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.auth.register.components.BottomSection
import com.example.wearzone.presentation.auth.register.components.LuxeTextField
import com.example.wearzone.presentation.auth.register.components.PasswordStrengthBar
import com.example.wearzone.presentation.auth.register.components.TermsCheckbox
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.toMessage

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEmailVerification: (String) -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val registerFailed = stringResource(R.string.error_register_failed)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RegisterUiEffect.NavigateToHome -> onNavigateToHome()
                is RegisterUiEffect.NavigateToLogin -> onNavigateToLogin()
                is RegisterUiEffect.NavigateToEmailVerification -> onNavigateToEmailVerification(effect.email)
                is RegisterUiEffect.ShowSnackbar -> {
                    val message = effect.error?.toMessage(context) ?: registerFailed
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        RegisterScreenContent(
            formState = formState,
            uiState = uiState,
            intentActions = viewModel::handleIntent,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun RegisterScreenContent(
    formState: RegisterFormState,
    uiState: RegisterUiState,
    intentActions: (RegisterUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.create_account),
            style = MaterialTheme.typography.headlineMedium,
            color = AppTheme.colors.textPrimary,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.register_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
        )

        Spacer(Modifier.height(32.dp))

        LuxeTextField(
            value = formState.name,
            onValueChange = { intentActions(RegisterUiIntent.NameChanged(it)) },
            label = stringResource(R.string.full_name),
            placeholder = stringResource(R.string.full_name_placeholder),
            isError = formState.nameError != null,
            errorMessage = formState.nameError,
        )

        Spacer(Modifier.height(20.dp))

        LuxeTextField(
            value = formState.email,
            onValueChange = { intentActions(RegisterUiIntent.EmailChanged(it)) },
            label = stringResource(R.string.email_address),
            placeholder = stringResource(R.string.email_placeholder),
            isError = formState.emailError != null,
            errorMessage = formState.emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
            ),
        )

        Spacer(Modifier.height(20.dp))

        LuxeTextField(
            value = formState.password,
            onValueChange = { intentActions(RegisterUiIntent.PasswordChanged(it)) },
            label = stringResource(R.string.password),
            placeholder = stringResource(R.string.password_placeholder),
            isError = formState.passwordError != null,
            errorMessage = formState.passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
            ),
            visualTransformation = if (formState.isPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),

            trailingIcon = {
                IconButton(
                    onClick = {
                        intentActions(RegisterUiIntent.TogglePasswordVisibility)
                    },
                    modifier = Modifier.size(20.dp),
                ) {

                    Icon(
                        imageVector = if (formState.isPasswordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,

                        contentDescription = if (formState.isPasswordVisible) stringResource(R.string.hide_password)
                        else stringResource(R.string.show_password),

                        tint = AppTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            },
        )

        Spacer(Modifier.height(8.dp))
        PasswordStrengthBar(password = formState.password)
        Spacer(Modifier.height(20.dp))
        LuxeTextField(
            value = formState.confirmPassword,
            onValueChange = { intentActions(RegisterUiIntent.ConfirmPasswordChanged(it)) },
            label = stringResource(R.string.confirm_password),
            placeholder = stringResource(R.string.password_placeholder),
            isError = formState.confirmPasswordError != null,
            errorMessage = formState.confirmPasswordError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (formState.isConfirmPasswordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),

            trailingIcon = {
                IconButton(
                    onClick = { intentActions(RegisterUiIntent.ToggleConfirmPasswordVisibility) },
                    modifier = Modifier.size(20.dp),
                ) {
                    Icon(
                        imageVector = if (formState.isConfirmPasswordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,

                        contentDescription = if (formState.isConfirmPasswordVisible) stringResource(
                            R.string.hide_password
                        )
                        else stringResource(R.string.show_password),
                        tint = AppTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            },
        )

        Spacer(Modifier.height(24.dp))

        TermsCheckbox(
            checked = formState.termsAccepted,
            onCheckedChange = { intentActions(RegisterUiIntent.TermsAccepted(it)) },
            onTermsClick = {},
            onPrivacyClick = {},
        )

        Spacer(Modifier.height(32.dp))
        val isLoading = uiState is RegisterUiState.Loading
        val canSubmit = formState.termsAccepted && !isLoading

        Button(
            onClick = { intentActions(RegisterUiIntent.SubmitRegister) },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.selected,
                contentColor = AppTheme.colors.onAccent,
                disabledContainerColor = AppTheme.colors.selected.copy(alpha = 0.5f),
                disabledContentColor = AppTheme.colors.onAccent.copy(alpha = 0.5f),
            ),
        ) {
            if (isLoading) {

                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = AppTheme.colors.onAccent,
                    strokeWidth = 2.dp,
                )

            } else {
                Text(
                    text = stringResource(
                        R.string.create_account,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        BottomSection {
            intentActions(RegisterUiIntent.NavigateToLoginClicked)
        }

    }
}
