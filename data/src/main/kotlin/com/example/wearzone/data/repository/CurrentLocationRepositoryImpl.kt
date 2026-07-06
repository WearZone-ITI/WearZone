package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ICurrentLocationDataSource
import com.example.wearzone.domain.common.runCatchingCancellable
import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.model.CurrentLocationPermissionDeniedException
import com.example.wearzone.domain.customer.address.model.CurrentLocationUnavailableException
import com.example.wearzone.domain.customer.address.repository.ICurrentLocationRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CurrentLocationRepositoryImpl @Inject constructor(
    private val localDataSource: ICurrentLocationDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICurrentLocationRepository {
    override suspend fun getCurrentCoordinates(): Result<AddressCoordinates> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                localDataSource.getCurrentCoordinates()
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toCurrentLocationException()) },
            )
        }

    private fun Throwable.toCurrentLocationException(): Throwable =
        when (this) {
            is CurrentLocationPermissionDeniedException,
            is CurrentLocationUnavailableException,
            -> this
            else -> CurrentLocationUnavailableException()
        }
}
