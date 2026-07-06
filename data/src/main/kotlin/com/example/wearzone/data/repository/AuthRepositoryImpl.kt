package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.IOnboardingPreferencesDataSource
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.data.remote.datasource.IAuthRemoteDataSource
import com.example.wearzone.domain.auth.model.User
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.common.runCatchingCancellable
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: IAuthRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val dataSource: IOnboardingPreferencesDataSource,
    private val settingsDataSource: ISettingsPreferencesDataSource
) : IAuthRepository {

    override suspend fun loginWithEmail(email: String, password: String): Result<User> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val firebaseUser = remoteDataSource.signInWithEmail(email, password)
                saveShopifyCustomerIdLocally(firebaseUser)
                firebaseUser.toDomain()
            }
        }

    override suspend fun loginWithGoogleCredential(idToken: String): Result<User> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val firebaseUser = remoteDataSource.signInWithGoogleCredential(idToken)
                saveShopifyCustomerIdLocally(firebaseUser)
                firebaseUser.toDomain()
            }
        }

    override suspend fun isLoggedIn(): Boolean = withContext(ioDispatcher) {
        remoteDataSource.getCurrentUser() != null
    }

    override suspend fun getCurrentUser(): User? = withContext(ioDispatcher) {
        remoteDataSource.getCurrentUser()?.toDomain()
    }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        runCatchingCancellable {
            remoteDataSource.signOut()
            settingsDataSource.setCustomerId(null)
            settingsDataSource.setDraftOrderId(null)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): Result<User> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val result = remoteDataSource.register(name, email, password)
            settingsDataSource.setCustomerId(result.customerId)
            settingsDataSource.setDraftOrderId(null)
            result.firebaseUser.toDomain()
        }
    }

    private suspend fun saveShopifyCustomerIdLocally(firebaseUser: FirebaseUser) {
        val shopifyCustomerId = remoteDataSource
            .getSavedShopifyCustomerId(firebaseUser.uid)
            ?.takeIf { it > 0L }
        settingsDataSource.setCustomerId(shopifyCustomerId)
        settingsDataSource.setDraftOrderId(null)
    }

    private fun FirebaseUser.toDomain(): User {
        return User(
            uid = this.uid,
            email = this.email ?: "",
            displayName = this.displayName,
            photoUrl = this.photoUrl?.toString()
        )
    }

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

    override suspend fun sendEmailVerification(): Result<Unit> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                remoteDataSource.sendEmailVerification()
            }
        }

    override suspend fun checkEmailVerified(): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                remoteDataSource.isEmailVerified()

                }
        }
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                remoteDataSource.sendPasswordResetEmail(email)
            }
        }

}
