package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.api.CurrencyApiService
import com.example.wearzone.domain.account.model.CurrencyRate
import com.example.wearzone.domain.account.repository.ICurrencyRepository
import com.example.wearzone.domain.common.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val apiService: CurrencyApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ICurrencyRepository {

    private val _rates = MutableStateFlow<List<CurrencyRate>>(emptyList())
    
    override suspend fun fetchRates(): Result<List<CurrencyRate>> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val response = apiService.getLatestRates("EGP")
            val ratesList = response.rates.map { (code, rate) ->
                CurrencyRate(code, rate)
            }
            _rates.value = ratesList
            ratesList
        }
    }

    override fun observeRates(): Flow<List<CurrencyRate>> = _rates.asStateFlow()

    override fun getRateFor(currencyCode: String): Double {
        return _rates.value.find { it.currencyCode == currencyCode }?.rate ?: 1.0
    }
}
