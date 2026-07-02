package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.datasource.IDiscountRemoteDataSource
import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import com.example.wearzone.domain.checkout.model.DiscountLookupException
import com.example.wearzone.domain.checkout.model.InvalidDiscountCodeException
import com.example.wearzone.domain.checkout.repository.IDiscountRepository
import com.example.wearzone.domain.common.runCatchingCancellable
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import javax.inject.Inject

class DiscountRepositoryImpl @Inject constructor(
    private val remoteDataSource: IDiscountRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IDiscountRepository {

    override suspend fun validateDiscountCode(code: String): Result<CheckoutDiscount> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                remoteDataSource.validateDiscountCode(code)
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toDiscountException()) },
            )
        }

    private fun Throwable.toDiscountException(): Throwable =
        when (this) {
            is InvalidDiscountCodeException -> this
            is HttpException -> toDiscountHttpException()
            is IOException -> DiscountLookupException(this)
            is SerializationException -> DiscountLookupException(this)
            else -> DiscountLookupException(this)
        }

    private fun HttpException.toDiscountHttpException(): Throwable =
        when (code()) {
            404 -> InvalidDiscountCodeException()
            else -> DiscountLookupException(this)
        }
}
