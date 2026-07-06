package com.example.wearzone.data.repository

import android.util.Log
import com.example.data.BuildConfig
import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.api.PaymobApiService
import com.example.wearzone.data.remote.dto.*
import com.example.wearzone.domain.checkout.repository.IPaymobRepository
import com.example.wearzone.domain.checkout.repository.PaymobBillingData
import com.example.wearzone.domain.common.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class PaymobRepositoryImpl @Inject constructor(
    private val apiService: PaymobApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IPaymobRepository {

    private val secretKey = BuildConfig.PAYMOB_API_KEY
    private val publicKey = BuildConfig.PAYMOB_PUBLIC_KEY
    private val integrationId = BuildConfig.PAYMOB_INTEGRATION_ID.toIntOrNull() ?: 0

    override suspend fun getPaymentToken(
        amount: Double,
        currency: String,
        billingData: PaymobBillingData
    ): Result<Pair<String, String>> = withContext(ioDispatcher) {
        runCatchingCancellable {
            try {
                val amountCents = (amount * 100).toLong()
                val response = apiService.createIntention(
                    authHeader = "Token $secretKey",
                    request = PaymobIntentionRequest(
                        amount = amountCents,
                        currency = currency,
                        paymentMethods = listOf(integrationId),
                        billingData = billingData.toDto(),
                    )
                )
                response.clientSecret to publicKey
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("PaymobError", "Code: ${e.code()} | Body: $errorBody")
                throw e
            }
        }
    }

    private fun PaymobBillingData.toDto() = PaymobBillingDataDto(
        apartment = apartment,
        email = email,
        floor = floor,
        first_name = firstName,
        street = street,
        building = building,
        phone_number = phoneNumber,
        city = city,
        country = country,
        last_name = lastName,
        state = state
    )
}
