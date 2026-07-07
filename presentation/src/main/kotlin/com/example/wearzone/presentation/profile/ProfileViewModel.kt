package com.example.wearzone.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.presentation.R
import com.example.wearzone.domain.account.model.OrderHistory
import com.example.wearzone.domain.account.usecase.GetOrderHistoryUseCase
import com.example.wearzone.domain.account.repository.ICurrencyRepository
import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.auth.usecase.LogoutUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCurrentCustomerIdUseCase
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.wearzone.domain.settings.usecase.SetLanguageUseCase
import com.example.wearzone.domain.settings.usecase.SetNotificationsEnabledUseCase
import com.example.wearzone.domain.settings.usecase.SetThemeModeUseCase
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val getOrderHistoryUseCase: GetOrderHistoryUseCase,
    private val getCurrentCustomerIdUseCase: GetCurrentCustomerIdUseCase,
    private val observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
    private val settingsRepository: ISettingsRepository,
    private val currencyRepository: ICurrencyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ProfileUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadProfile()
        observeCart()
        observeSettings()
        fetchCurrencyRates()
    }

    fun handleIntent(intent: ProfileUiIntent) {
        when (intent) {
            ProfileUiIntent.OnMyOrdersClicked -> navigateIfAuthenticated(ProfileUiEffect.NavigateToOrders)
            ProfileUiIntent.OnWishlistClicked -> navigateIfAuthenticated(ProfileUiEffect.NavigateToWishlist)
            ProfileUiIntent.OnSavedAddressesClicked -> navigateIfAuthenticated(ProfileUiEffect.NavigateToSavedAddresses)
            ProfileUiIntent.OnCurrencyClicked -> Unit // Handled by UI
            is ProfileUiIntent.OnCurrencySelected -> updateCurrency(intent.currencyCode)
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
                _uiEffect.send(ProfileUiEffect.ShowSignInRequired)
                return@launch
            }
            
            val user = getCurrentUserUseCase()
            val customerIdResult = getCurrentCustomerIdUseCase()
            
            val recentOrders = customerIdResult.getOrNull()?.let { id ->
                getOrderHistoryUseCase(id).getOrNull()?.take(2)?.map { it.toRecentOrderUi() }
            } ?: emptyList()

            _uiState.value = ProfileUiState.Content(
                displayName = user?.displayName,
                email = user?.email,
                photoUrl = user?.photoUrl,
                recentOrders = recentOrders.toImmutableList(),
                isAuthenticated = user != null,
            )
        }
    }

    private fun OrderHistory.toRecentOrderUi(): RecentOrderUiModel {
        val firstItem = lineItems.firstOrNull()
        return RecentOrderUiModel(
            id = id.toString(),
            statusLabel = financialStatus?.replaceFirstChar { it.uppercase() },
            title = firstItem?.title ?: "",
            imageUrl = firstItem?.imageUrl,
        )
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
                val count = if (getAuthAccessStateUseCase() is AuthAccessState.AuthenticatedCustomer) {
                    cartItems.sumOf { it.quantity }
                } else {
                    0
                }
                _uiState.update { state ->
                    when (state) {
                        is ProfileUiState.Content -> state.copy(cartItemCount = count)
                        is ProfileUiState.Guest -> state.copy(cartItemCount = 0)
                        else -> state
                    }
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            observeSettingsPreferencesUseCase().collect { preferences ->
                _uiState.update { state ->
                    if (state is ProfileUiState.Content) {
                        state.copy(selectedCurrency = preferences.selectedCurrency)
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun fetchCurrencyRates() {
        viewModelScope.launch {
            currencyRepository.fetchRates()
        }
    }

    private fun updateCurrency(currencyCode: String) {
        viewModelScope.launch {
            settingsRepository.setCurrency(currencyCode)
        }
    }
}
