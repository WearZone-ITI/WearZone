package com.example.wearzone.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

data class CurrencyState(
    val selectedCurrency: String = "EGP",
    val exchangeRate: Double = 1.0
)

val LocalCurrencyState = staticCompositionLocalOf { CurrencyState() }

@Composable
fun formatPrice(basePriceEgp: Double): String {
    val state = LocalCurrencyState.current
    val convertedPrice = basePriceEgp * state.exchangeRate
    
    return when (state.selectedCurrency) {
        "USD" -> "$${String.format(Locale.US, "%.2f", convertedPrice)}"
        "EUR" -> "€${String.format(Locale.US, "%.2f", convertedPrice)}"
        "GBP" -> "£${String.format(Locale.US, "%.2f", convertedPrice)}"
        "EGP" -> "${String.format(Locale.US, "%.2f", convertedPrice)} EGP"
        else -> "${String.format(Locale.US, "%.2f", convertedPrice)} ${state.selectedCurrency}"
    }
}
