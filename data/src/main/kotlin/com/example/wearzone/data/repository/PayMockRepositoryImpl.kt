package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.IPayMockLocalDataSource
import com.example.wearzone.data.remote.api.PayMockApiService
import com.example.wearzone.data.remote.dto.PayMockPaymentRequestDto
import com.example.wearzone.data.remote.dto.PayMockProjectRequestDto
import com.example.wearzone.data.remote.dto.toDomain
import com.example.wearzone.domain.checkout.model.PayMockPaymentResponse
import com.example.wearzone.domain.checkout.repository.IPayMockRepository
import com.example.wearzone.domain.common.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class PayMockRepositoryImpl @Inject constructor(
    private val apiService: PayMockApiService,
    private val localDataSource: IPayMockLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IPayMockRepository {

    override suspend fun processPayment(
        amount: Double,
        currency: String
    ): Result<PayMockPaymentResponse> = withContext(ioDispatcher) {
        runCatchingCancellable {
            ensureProjectAndGetApiKey()
            
            try {
                executePayment(amount, currency)
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403) {
                    // ApiKey might be invalid/expired, clear it, recreate project and retry once
                    localDataSource.clearApiKey()
                    ensureProjectAndGetApiKey()
                    executePayment(amount, currency)
                } else {
                    throw e
                }
            }
        }
    }

    private suspend fun ensureProjectAndGetApiKey(): String {
        val storedKey = localDataSource.getApiKey()
        if (!storedKey.isNullOrBlank()) return storedKey

        val projectResponse = apiService.createProject(
            PayMockProjectRequestDto(name = "WearZone")
        )
        localDataSource.saveApiKey(projectResponse.apiKey)
        return projectResponse.apiKey
    }

    private suspend fun executePayment(amount: Double, currency: String): PayMockPaymentResponse {
        return apiService.processPayment(
            PayMockPaymentRequestDto(amount = amount, currency = currency)
        ).toDomain()
    }
}
