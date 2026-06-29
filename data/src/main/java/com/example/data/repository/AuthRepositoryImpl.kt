package com.example.data.repository

import com.example.data.local.datastore.IOnboardingPreferencesDataSource
import com.example.data.remote.datasource.IAuthRemoteDataSource
import com.example.domain.auth.model.User
import com.example.domain.auth.repository.IAuthRepository
import com.example.domain.common.result.runCatchingCancellable
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: IAuthRemoteDataSource,
    private val dataSource: IOnboardingPreferencesDataSource,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) : IAuthRepository {

    override suspend fun loginWithEmail(email: String, password: String): Result<User> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                remoteDataSource.signInWithEmail(email, password).toDomain()
            }
        }

    override suspend fun loginWithGoogleCredential(idToken: String): Result<User> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                remoteDataSource.signInWithGoogleCredential(idToken).toDomain()
            }
        }

    override suspend fun isLoggedIn(): Boolean =
        withContext(ioDispatcher) {
            remoteDataSource.getCurrentUser() != null
        }

    override suspend fun getCurrentUser(): User? =
        withContext(ioDispatcher) {
            remoteDataSource.getCurrentUser()?.toDomain()
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
}
