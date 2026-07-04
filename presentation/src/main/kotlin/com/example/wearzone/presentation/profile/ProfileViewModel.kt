package com.example.wearzone.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.auth.usecase.LogoutUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ProfileUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadProfile()
        observeCart()
    }

    fun handleIntent(intent: ProfileUiIntent) {
        when (intent) {
            ProfileUiIntent.OnMyOrdersClicked -> navigateIfAuthenticated(ProfileUiEffect.NavigateToOrders)
            ProfileUiIntent.OnWishlistClicked -> navigateIfAuthenticated(ProfileUiEffect.NavigateToWishlist)
            ProfileUiIntent.OnSavedAddressesClicked -> navigateIfAuthenticated(ProfileUiEffect.NavigateToSavedAddresses)
            ProfileUiIntent.OnCurrencyClicked -> sendEffect(ProfileUiEffect.ShowError(R.string.profile_currency_todo))
            ProfileUiIntent.OnSettingsClicked -> sendEffect(ProfileUiEffect.NavigateToSettings)
            ProfileUiIntent.OnLogoutClicked -> requestLogout()
            ProfileUiIntent.OnLogoutConfirmed -> confirmLogout()
            ProfileUiIntent.OnLogoutCancelled -> Unit
            ProfileUiIntent.OnRetry -> loadProfile()
            ProfileUiIntent.OnCardClicked -> sendEffect(ProfileUiEffect.NavigateToCart)
            ProfileUiIntent.OnSignInClicked -> sendEffect(ProfileUiEffect.NavigateToLogin)
            ProfileUiIntent.OnCreateAccountClicked -> sendEffect(ProfileUiEffect.NavigateToRegister)
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            if (getAuthAccessStateUseCase() !is AuthAccessState.AuthenticatedCustomer) {
                _uiState.value = ProfileUiState.Guest()
                return@launch
            }
            val user = getCurrentUserUseCase()
            _uiState.value = ProfileUiState.Content(
                displayName = user?.displayName,
                email = user?.email,
                photoUrl = user?.photoUrl,
                recentOrders = profileRecentOrders(),
                isAuthenticated = user != null,
            )
        }
    }

    private fun navigateIfAuthenticated(effect: ProfileUiEffect) {
        val state = _uiState.value as? ProfileUiState.Content
        if (state?.isAuthenticated == true) {
            sendEffect(effect)
        } else {
            sendEffect(ProfileUiEffect.ShowSignInRequired)
        }
    }

    private fun requestLogout() {
        sendEffect(ProfileUiEffect.ShowLogoutConfirmation)
    }

    private fun confirmLogout() {
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess {
                    _uiEffect.send(ProfileUiEffect.NavigateToLogin)
                }
                .onFailure {
                    _uiEffect.send(ProfileUiEffect.ShowError(R.string.profile_logout_failed))
                }
        }
    }

    private fun sendEffect(effect: ProfileUiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            observeCartUseCase().collect { cartItems ->
                val count = cartItems.sumOf { it.quantity }
                _uiState.update { state ->
                    when (state) {
                        is ProfileUiState.Content -> state.copy(cartItemCount = count)
                        is ProfileUiState.Guest -> state.copy(cartItemCount = count)
                        else -> state
                    }
                }
            }
        }
    }
}
