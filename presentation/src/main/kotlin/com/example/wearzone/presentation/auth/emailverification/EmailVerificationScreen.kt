package com.example.wearzone.presentation.auth.emailverification

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.OutlinedButtonDefaults
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.theme.AppTypography

@Composable
fun EmailVerificationScreen(
    viewModel: EmailVerificationViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is EmailVerificationUiEffect.NavigateToHome -> onNavigateToHome()
                is EmailVerificationUiEffect.NavigateToLogin -> onNavigateToLogin()
                is EmailVerificationUiEffect.ShowSnackbar -> {
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
        EmailVerificationContent(
            uiState = uiState,
            screenState = screenState,
            onIntent = viewModel::handleIntent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun EmailVerificationContent(
    uiState: EmailVerificationUiState,
    screenState: EmailVerificationScreenState,
    onIntent: (EmailVerificationUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(AppTheme.colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.verify_email_title),
                style = AppTypography.titleLarge,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.verify_email_subtitle, screenState.email),
                style = AppTypography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            if (uiState is EmailVerificationUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.message,
                    style = AppTypography.bodyMedium,
                    color = AppTheme.colors.error,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onIntent(EmailVerificationUiIntent.OnCheckStatusClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.selected,
                    contentColor = AppTheme.colors.onAccent,
                ),
            ) {
                if (screenState.isChecking) {
                    CircularProgressIndicator(color = AppTheme.colors.onAccent)
                } else {
                    Text(
                        text = stringResource(R.string.ive_verified_continue),
                        style = AppTypography.labelMedium,
                        color = AppTheme.colors.onAccent,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onIntent(EmailVerificationUiIntent.OnResendClicked) },
                enabled = screenState.resendCooldownSeconds == 0 && !screenState.isResending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppTheme.colors.textPrimary,
                ),
            ) {
                val resendText = if (screenState.resendCooldownSeconds > 0) {
                    stringResource(R.string.resend_email_countdown, screenState.resendCooldownSeconds)
                } else {
                    stringResource(R.string.resend_email)
                }
                Text(
                    text = resendText,
                    style = AppTypography.labelMedium,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.logout),
                    style = AppTypography.labelMedium,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.clickable {
                        onIntent(EmailVerificationUiIntent.OnLogoutClicked)
                    }
                )
            }
        }
    }
}
