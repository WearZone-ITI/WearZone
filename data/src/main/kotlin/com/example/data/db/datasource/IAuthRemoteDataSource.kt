package com.example.data.db.datasource

import com.google.firebase.auth.FirebaseUser

interface IAuthRemoteDataSource {
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser
    suspend fun signInWithGoogleCredential(idToken: String): FirebaseUser
    suspend fun register(name: String, email: String, password: String): FirebaseUser
    fun getCurrentUser(): FirebaseUser?
}