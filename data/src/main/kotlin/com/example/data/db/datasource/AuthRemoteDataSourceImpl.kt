package com.example.data.db.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : IAuthRemoteDataSource {

    override suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("User is null after sign in")
    }

    override suspend fun signInWithGoogleCredential(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        return result.user ?: throw Exception("User is null after Google sign in")
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): FirebaseUser {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
            ?: throw IllegalStateException("Firebase user was null after registration")

        return firebaseUser
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}