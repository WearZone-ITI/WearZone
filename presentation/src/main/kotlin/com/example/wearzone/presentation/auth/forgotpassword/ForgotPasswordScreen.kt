package com.example.wearzone.presentation.auth.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.auth.register.components.LuxeTextField
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.theme.AppTypography

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ForgotPasswordUiEffect.NavigateToLogin -> onNavigateToLogin()
                is ForgotPasswordUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = AppTheme.colors.selected,
                    contentColor = AppTheme.colors.onAccent,
                )
            }
        },
        containerColor = AppTheme.colors.background,
    ) { paddingValues ->
        ForgotPasswordContent(
            uiState = uiState,
            formState = formState,
            onIntent = viewModel::handleIntent,
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    formState: ForgotPasswordFormState,
    onIntent: (ForgotPasswordUiIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(AppTheme.colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = AppTheme.colors.textPrimary,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                if (uiState is ForgotPasswordUiState.EmailSent) {
                    Text(
                        text = stringResource(R.string.reset_email_sent_title),
                        style = AppTypography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.reset_email_sent_message,
                            formState.email.trim(),
                        ),
                        style = AppTypography.bodyLarge,
                        color = AppTheme.colors.textSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onIntent(ForgotPasswordUiIntent.OnBackToLoginClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.selected,
                            contentColor = AppTheme.colors.onAccent,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.back_to_login),
                            style = AppTypography.labelMedium,
                            color = AppTheme.colors.onAccent,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.forgot_password_title),
                        style = AppTypography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.forgot_password_subtitle),
                        style = AppTypography.bodyLarge,
                        color = AppTheme.colors.textSecondary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    LuxeTextField(
                        value = formState.email,
                        onValueChange = {
                            onIntent(ForgotPasswordUiIntent.OnEmailChanged(it))
                        },
                        label = stringResource(R.string.email_label),
                        placeholder = stringResource(R.string.email_placeholder),
                        isError = uiState is ForgotPasswordUiState.Error,
                        errorMessage = (uiState as? ForgotPasswordUiState.Error)?.message,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onIntent(ForgotPasswordUiIntent.OnSendResetLinkClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.selected,
                            contentColor = AppTheme.colors.onAccent,
                        ),
                    ) {
                        if (uiState is ForgotPasswordUiState.Loading) {
                            CircularProgressIndicator(color = AppTheme.colors.onAccent)
                        } else {
                            Text(
                                text = stringResource(R.string.send_reset_link),
                                style = AppTypography.labelMedium,
                                color = AppTheme.colors.onAccent,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.back_to_login),
                            style = AppTypography.labelMedium,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.clickable {
                                onIntent(ForgotPasswordUiIntent.OnBackToLoginClicked)
                            }
                        )
                    }
                }
            }
        }
    }
}
