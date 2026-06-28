package com.example.data.remote.datasource

import com.google.firebase.auth.FirebaseUser

interface IAuthRemoteDataSource {
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser
    suspend fun signInWithGoogleCredential(idToken: String): FirebaseUser
    fun getCurrentUser(): FirebaseUser?
}
