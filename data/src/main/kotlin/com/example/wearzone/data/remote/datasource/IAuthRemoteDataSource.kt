package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.result.RegisterResult
import com.google.firebase.auth.FirebaseUser

interface IAuthRemoteDataSource {
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser
    suspend fun signInWithGoogleCredential(idToken: String): FirebaseUser
    suspend fun getSavedShopifyCustomerId(uid: String): Long?
    suspend fun register(name: String, email: String, password: String): RegisterResult
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
}