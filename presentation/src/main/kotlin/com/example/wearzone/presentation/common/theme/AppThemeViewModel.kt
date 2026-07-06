package com.example.wearzone.presentation.common.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearzone.domain.account.repository.ICurrencyRepository
import com.example.wearzone.domain.settings.model.SettingsPreferences
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.wearzone.presentation.common.CurrencyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    observeSettingsPreferencesUseCase: ObserveSettingsPreferencesUseCase,
    private val currencyRepository: ICurrencyRepository,
) : ViewModel() {

    val settingsPreferences: StateFlow<SettingsPreferences> =
        observeSettingsPreferencesUseCase()
            .catch { emit(SettingsPreferences()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsPreferences(),
            )

    val currencyState: StateFlow<CurrencyState> = combine(
        settingsPreferences,
        currencyRepository.observeRates()
    ) { preferences, rates ->
        val currency = preferences.selectedCurrency
        val rate = rates.find { it.currencyCode == currency }?.rate ?: 1.0
        CurrencyState(currency, rate)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CurrencyState()
    )
}
