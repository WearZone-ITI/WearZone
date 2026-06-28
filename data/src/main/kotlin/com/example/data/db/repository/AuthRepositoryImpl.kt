package com.example.data.db.repository

import com.example.data.db.di.IoDispatcher
import com.example.domain.auth.model.User
import com.example.domain.auth.repository.IAuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IAuthRepository {

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Firebase user was null after registration")

            User(
                uid = firebaseUser.uid,
                email = email,
                displayName = name,
            )
        }
    }
}
