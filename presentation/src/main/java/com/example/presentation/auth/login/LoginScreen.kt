package com.example.presentation.auth.login

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.auth.login.components.LoginForm
import com.example.presentation.auth.login.components.SocialLoginButtons
import com.example.presentation.common.theme.AppColors
import com.example.presentation.common.theme.AppTypography
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LoginUiEffect.NavigateToHome -> onLoginSuccess()
                is LoginUiEffect.NavigateToRegister -> onNavigateToRegister()
                is LoginUiEffect.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
                is LoginUiEffect.LaunchGoogleSignIn -> {
                    scope.launch {
                        try {
                            val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(context.getString(com.example.presentation.R.string.default_web_client_id))
                                .setAutoSelectEnabled(true)
                                .build()

                            val request = androidx.credentials.GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(
                                request = request,
                                context = context
                            )
                            
                            val credential = result.credential
                            if (credential is androidx.credentials.CustomCredential &&
                                credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                viewModel.handleIntent(LoginUiIntent.OnGoogleSignInResult(googleIdTokenCredential.idToken))
                            } else {
                                snackbarHostState.showSnackbar("Unexpected credential type")
                            }
                        } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                            snackbarHostState.showSnackbar(e.message ?: "Google Sign-In failed")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(e.message ?: "Google Sign-In failed")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
    Box(modifier = modifier.fillMaxSize().background(AppColors.Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "WearZone",
                style = AppTypography.headlineLarge,
                color = AppColors.TextPrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Welcome Back",
                style = AppTypography.titleLarge,
                color = AppColors.TextPrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to access your curated collection.",
                style = AppTypography.bodyLarge,
                color = AppColors.TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))

            LoginForm(
                formState = formState,
                onIntent = onIntent
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Forgot Password?",
                style = AppTypography.labelMedium,
                color = AppColors.TextSecondary,
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
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                enabled = uiState !is LoginUiState.Loading
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(color = AppColors.OnPrimary)
                } else {
                    Text("Sign In", style = AppTypography.labelMedium, color = AppColors.OnPrimary)
                }
            }

            if (uiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.message,
                    style = AppTypography.bodyMedium,
                    color = AppColors.Error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.Divider)
                Text(
                    text = "OR CONTINUE WITH",
                    style = AppTypography.labelSmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.Divider)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SocialLoginButtons(onIntent = onIntent)

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = AppTypography.labelMedium,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "Register",
                    style = AppTypography.labelMedium,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.clickable { onIntent(LoginUiIntent.OnRegisterClicked) }
                )
            }
        }
    }
}
