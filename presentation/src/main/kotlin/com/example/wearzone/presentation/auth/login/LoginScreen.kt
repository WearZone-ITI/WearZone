package com.example.wearzone.presentation.auth.login

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.auth.login.components.LoginForm
import com.example.wearzone.presentation.auth.login.components.SocialLoginButtons
import com.example.wearzone.presentation.common.NetworkStatusBanner
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.theme.AppTypography
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    googleWebClientId: String,
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToEmailVerification: (String) -> Unit,
    onNavigateToForgotPassword: () -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }

    val loginSuccessful = stringResource(R.string.login_successful)
    val unexpectedCredential = stringResource(R.string.unexpected_credential_type)
    val googleFailed = stringResource(R.string.google_sign_in_failed)
    val googleNotConfigured = stringResource(R.string.google_sign_in_not_configured)

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LoginUiEffect.NavigateToHome -> onNavigateToHome()
                is LoginUiEffect.NavigateToRegister -> { onNavigateToRegister() }
                is LoginUiEffect.NavigateToEmailVerification -> onNavigateToEmailVerification(effect.email)
                is LoginUiEffect.NavigateToForgotPassword -> onNavigateToForgotPassword()
                is LoginUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LoginUiEffect.LaunchGoogleSignIn -> {
                    try {
                        if (googleWebClientId.isBlank()) {
                            snackbarHostState.showSnackbar(googleNotConfigured)
                            return@collect
                        }

                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(googleWebClientId)
                            .setAutoSelectEnabled(true)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                        val credential = result.credential
                        if (credential is CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            viewModel.handleIntent(LoginUiIntent.OnGoogleSignInResult(googleIdTokenCredential.idToken))
                        } else { snackbarHostState.showSnackbar(unexpectedCredential) }
                    } catch (e: GetCredentialException) {
                        snackbarHostState.showSnackbar(e.message ?: googleFailed)
                    } catch (e: Exception) { snackbarHostState.showSnackbar(e.message ?: googleFailed) }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("success", ignoreCase = true)
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isSuccess) AppTheme.colors.success
                    else AppTheme.colors.selected,
                    contentColor = AppTheme.colors.onAccent,
                )
            }
        },
        containerColor = AppTheme.colors.background,
    ) { paddingValues ->
        LoginContent(
            uiState = uiState,
            formState = formState,
            onIntent = viewModel::handleIntent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    formState: LoginFormState,
    onIntent: (LoginUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(AppTheme.colors.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = AppTypography.headlineLarge,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.welcome_back),
                style = AppTypography.titleLarge,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = AppTypography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))

            LoginForm(
                formState = formState,
                onIntent = onIntent
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.forgot_password),

                style = AppTypography.labelMedium,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onIntent(LoginUiIntent.OnForgotPasswordClicked) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onIntent(LoginUiIntent.OnSignInClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.selected,
                    contentColor = AppTheme.colors.onAccent,
                ),
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(color = AppTheme.colors.onAccent)
                } else {
                    Text(text = stringResource(R.string.sign_in),
                        style = AppTypography.labelMedium,
                        color = AppTheme.colors.onAccent,
                    )
                }
            }

            if (uiState is LoginUiState.Error) {
                val isWarning = uiState.message.contains("fill", ignoreCase = true) || uiState.message.contains("empty", ignoreCase = true)
                if (isWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        style = AppTypography.bodyMedium,
                        color = AppTheme.colors.warning
                    )
                } else {
                    NetworkStatusBanner(message = uiState.message)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(Modifier.weight(1f))
                Text(text = stringResource(R.string.or_continue_with),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = AppTypography.labelSmall,
                    color = AppTheme.colors.textSecondary,
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.divider)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SocialLoginButtons(onIntent = onIntent)

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.dont_have_account) + " ",
                    style = AppTypography.labelMedium,
                    color = AppTheme.colors.textSecondary,
                )

                Text(text = stringResource(R.string.register),
                    style = AppTypography.labelMedium,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.clickable { onIntent(LoginUiIntent.OnRegisterClicked) }
                )
            }

            Spacer(Modifier.padding(12.dp))

            TextButton(
                onClick = { onIntent(LoginUiIntent.OnJoinAsGuestClicked) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_continue_as_guest),
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
