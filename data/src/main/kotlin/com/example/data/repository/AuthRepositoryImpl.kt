package com.example.data.repository

import com.example.data.local.datastore.IOnboardingPreferencesDataSource
import com.example.domain.onboarding.repository.IAuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val dataSource: IOnboardingPreferencesDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : IAuthRepository {

    override fun observeOnboardingCompleted(): Flow<Boolean> =
        dataSource.observeOnboardingCompleted()

    override suspend fun setOnboardingCompleted(completed: Boolean): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                dataSource.setOnboardingCompleted(completed)
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
            }
        }
}
