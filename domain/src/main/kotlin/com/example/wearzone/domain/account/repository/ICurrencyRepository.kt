package com.example.wearzone.domain.account.repository

import com.example.wearzone.domain.account.model.CurrencyRate
import kotlinx.coroutines.flow.Flow

interface ICurrencyRepository {
    suspend fun fetchRates(): Result<List<CurrencyRate>>
    fun observeRates(): Flow<List<CurrencyRate>>
    fun getRateFor(currencyCode: String): Double
}
