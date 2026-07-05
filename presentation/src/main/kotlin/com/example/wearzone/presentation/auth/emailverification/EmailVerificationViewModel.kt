package com.example.wearzone.presentation.auth.emailverification

import androidx.compose.ui.geometry.RoundRect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.usecase.CheckEmailVerifiedUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.auth.usecase.LogoutUseCase
import com.example.wearzone.domain.auth.usecase.SendEmailVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RESEND_COOLDOWN_SECONDS = 30
private const val AUTO_CHECK_INTERVAL_MS = 5000L

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val checkEmailVerifiedUseCase: CheckEmailVerifiedUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmailVerificationUiState>(EmailVerificationUiState.NotVerified)
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    private val _screenState = MutableStateFlow(EmailVerificationScreenState())
    val screenState: StateFlow<EmailVerificationScreenState> = _screenState.asStateFlow()

    private val _uiEffect = Channel<EmailVerificationUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private var cooldownJob: Job? = null
    private var pollingJob: Job? = null
    private var verifiedHandled = false

    init {
        viewModelScope.launch {
            val email = getCurrentUserUseCase()?.email.orEmpty()
            _screenState.update { it.copy(email = email) }
        }
        startCooldown()
        startAutoPolling()
    }

    fun handleIntent(intent: EmailVerificationUiIntent) {
        when (intent) {
            is EmailVerificationUiIntent.OnResendClicked -> resendVerificationEmail()
            is EmailVerificationUiIntent.OnCheckStatusClicked -> checkVerificationStatus(showErrorOnFalse = true)
            is EmailVerificationUiIntent.OnLogoutClicked -> logout()
        }
    }

    private fun resendVerificationEmail() {
        if (_screenState.value.resendCooldownSeconds > 0 || _screenState.value.isResending) return

        viewModelScope.launch {
            _screenState.update { it.copy(isResending = true) }
            sendEmailVerificationUseCase()
                .onSuccess {
                    sendEffect(EmailVerificationUiEffect.ShowSnackbar(R.string.email_verification_sent.toString()))
                    startCooldown()
                }
                .onFailure { error ->
                    sendEffect(
                        EmailVerificationUiEffect.ShowSnackbar(
                            error.localizedMessage ?: R.string.email_verification_failed_send.toString()
                        )
                    )
                }
            _screenState.update { it.copy(isResending = false) }
        }
    }

    private fun checkVerificationStatus(showErrorOnFalse: Boolean) {
        if (_screenState.value.isChecking) return

        viewModelScope.launch {
            _screenState.update { it.copy(isChecking = true) }
            checkEmailVerifiedUseCase()
                .onSuccess { isVerified ->
                    if (isVerified) {
                        onVerified()
                    } else if (showErrorOnFalse) {
                        sendEffect(
                            EmailVerificationUiEffect.ShowSnackbar("Email is not verified yet")
                        )
                    }
                }
                .onFailure { error ->
                    if (showErrorOnFalse) {
                        _uiState.value = EmailVerificationUiState.Error(
                            error.localizedMessage ?: "Failed to check verification status"
                        )
                    }
                }
            _screenState.update { it.copy(isChecking = false) }
        }
    }

    private fun onVerified() {
        if (verifiedHandled) return
        verifiedHandled = true
        pollingJob?.cancel()
        _uiState.value = EmailVerificationUiState.Verified
        sendEffect(EmailVerificationUiEffect.NavigateToHome)
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            sendEffect(EmailVerificationUiEffect.NavigateToLogin)
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (remaining in RESEND_COOLDOWN_SECONDS downTo 0) {
                _screenState.update { it.copy(resendCooldownSeconds = remaining) }
                if (remaining > 0) delay(1000)
            }
        }
    }

    private fun startAutoPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(AUTO_CHECK_INTERVAL_MS)
                checkVerificationStatus(showErrorOnFalse = false)
            }
        }
    }

    private fun sendEffect(effect: EmailVerificationUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
