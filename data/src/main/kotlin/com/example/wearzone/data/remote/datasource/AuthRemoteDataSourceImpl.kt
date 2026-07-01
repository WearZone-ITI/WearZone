package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.AuthApiService
import com.example.wearzone.data.remote.dto.CustomerDto
import com.example.wearzone.data.remote.dto.CustomerRequest
import com.example.wearzone.data.remote.result.RegisterResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authApiService: AuthApiService,
    private val firestore: FirebaseFirestore,
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

    override suspend fun getSavedShopifyCustomerId(uid: String): Long? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        return when (val value = snapshot.get("customerId")) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): RegisterResult {

        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw IllegalStateException("User creation failed")

            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
            user.updateProfile(profileUpdates).await()
            user.reload().await()

            val request = CustomerRequest(
                customer = CustomerDto(
                    firstName = name,
                    lastName = "",
                    email = email,
                    verifiedEmail = true,
                    sendEmailWelcome = false
                )
            )
            val customerResponse = authApiService.createCustomer(request)

            saveCustomerIdInFireStore(uid = user.uid, customerId = customerResponse.customer.id)

            return RegisterResult(firebaseUser = user, customerId = customerResponse.customer.id)
        } catch (e: Exception) {
            // rollback Firebase if Shopify fails
            firebaseAuth.currentUser?.delete()?.await()
            throw e
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private suspend fun saveCustomerIdInFireStore(
        uid: String,
        customerId: Long,
    ) {
        val data = mapOf("customerId" to customerId)
        firestore.collection("users").document(uid).set(data).await()
    }
}