package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.api.PaymobApiService
import com.example.wearzone.data.remote.mapper.toDomain
import com.example.wearzone.data.remote.mapper.toIntentionRequestDto
import com.example.wearzone.domain.checkout.model.CheckoutData
import com.example.wearzone.domain.checkout.model.PaymentIntention
import com.example.wearzone.domain.checkout.repository.IPaymentRepository
import com.example.wearzone.domain.common.PaymentException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class PaymentRepositoryImpl @Inject constructor(
    private val apiService: PaymobApiService,
    @Named("secretKey") private val secretKey: String,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : IPaymentRepository {

    override suspend fun createIntention(checkoutData: CheckoutData): Result<PaymentIntention> {

        return withContext(dispatcher) {
            try {
                val specialRef = "wearzone_${System.currentTimeMillis()}"
                val requestDto = checkoutData.toIntentionRequestDto(specialRef)
                val response = apiService.createIntention(
                    authHeader = "Token $secretKey",
                    request = requestDto
                )
                Result.success(response.toDomain())
            } catch (e: HttpException) {
                Result.failure(PaymentException.ApiError(e.code(), e.message()))
            } catch (e: IOException) {
                Result.failure(PaymentException.NetworkError)
            } catch (e: Exception) {
                Result.failure(PaymentException.Unknown(e.message))
            }
        }
    }
}