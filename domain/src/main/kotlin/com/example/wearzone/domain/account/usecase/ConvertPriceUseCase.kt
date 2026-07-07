package com.example.wearzone.domain.account.usecase

import com.example.wearzone.domain.account.repository.ICurrencyRepository
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

class ConvertPriceUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository,
    private val currencyRepository: ICurrencyRepository
) {
    operator fun invoke(basePriceEgp: Double): Flow<String> =
        settingsRepository.observeSettingsPreferences().map { preferences ->
            val currencyCode = preferences.selectedCurrency
            val rate = currencyRepository.getRateFor(currencyCode)
            val convertedPrice = basePriceEgp * rate
            formatPrice(convertedPrice, currencyCode)
        }

    private fun formatPrice(price: Double, currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "$${String.format(Locale.US, "%.2f", price)}"
            "EUR" -> "€${String.format(Locale.US, "%.2f", price)}"
            "GBP" -> "£${String.format(Locale.US, "%.2f", price)}"
            "EGP" -> "${String.format(Locale.US, "%.2f", price)} EGP"
            else -> "${String.format(Locale.US, "%.2f", price)} $currencyCode"
        }
    }
}
