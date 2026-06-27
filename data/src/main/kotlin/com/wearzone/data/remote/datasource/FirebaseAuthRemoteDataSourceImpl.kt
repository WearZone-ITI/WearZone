package com.wearzone.data.remote.datasource

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.wearzone.data.di.IoDispatcher
import com.wearzone.data.remote.dto.AuthUserDto
import com.wearzone.data.remote.interceptor.ServerException
import com.wearzone.data.remote.interceptor.ShopifyHttpException
import com.wearzone.data.remote.interceptor.UnauthorizedException
import com.wearzone.data.remote.interceptor.ValidationException
import com.wearzone.domain.common.result.runCatchingCancellable
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class FirebaseAuthRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IAuthRemoteDataSource {

    override suspend fun loginWithEmail(
        email: String,
        password: String,
    ): Result<AuthUserDto> = withContext(ioDispatcher) {
        runCatchingCancellable {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .await()
                .user
                ?.toAuthUserDto()
                ?: throw UnauthorizedException()
        }.mapFirebaseFailure()
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
    ): Result<AuthUserDto> = withContext(ioDispatcher) {
        runCatchingCancellable {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .await()
                .user
                ?.toAuthUserDto()
                ?: throw UnauthorizedException()
        }.mapFirebaseFailure()
    }

    private fun FirebaseUser.toAuthUserDto(): AuthUserDto =
        AuthUserDto(
            uid = uid,
            email = email,
            displayName = displayName,
            phoneNumber = phoneNumber,
            isEmailVerified = isEmailVerified,
        )

    private fun <T> Result<T>.mapFirebaseFailure(): Result<T> =
        fold(
            onSuccess = { value -> Result.success(value) },
            onFailure = { exception -> Result.failure(exception.toShopifyHttpException()) },
        )

    private fun Throwable.toShopifyHttpException(): Throwable =
        when (this) {
            is ShopifyHttpException -> this
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException -> UnauthorizedException(this)
            is FirebaseAuthUserCollisionException,
            is FirebaseAuthWeakPasswordException -> ValidationException(message, this)
            is FirebaseNetworkException -> ServerException(SERVICE_UNAVAILABLE, this)
            is FirebaseTooManyRequestsException -> ServerException(TOO_MANY_REQUESTS, this)
            is FirebaseException -> ServerException(INTERNAL_SERVER_ERROR, this)
            else -> this
        }

    private companion object {
        const val INTERNAL_SERVER_ERROR = 500
        const val SERVICE_UNAVAILABLE = 503
        const val TOO_MANY_REQUESTS = 429
    }
}
