package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.AuthApiService
import com.example.wearzone.data.remote.dto.CustomerDto
import com.example.wearzone.data.remote.dto.CustomerRequest
import com.example.wearzone.data.remote.result.RegisterResult
import com.example.wearzone.domain.common.FirebaseAuthFailureException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class AuthRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val authApiService: AuthApiService,
    private val firestore: FirebaseFirestore,
) : IAuthRemoteDataSource {

    override suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val result = mapFirebaseAuthException {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        }
        return result.user ?: throw IllegalStateException("firebase_user_missing_after_sign_in")
    }

    override suspend fun signInWithGoogleCredential(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = mapFirebaseAuthException {
            firebaseAuth.signInWithCredential(credential).await()
        }
        return result.user ?: throw IllegalStateException("firebase_user_missing_after_google_sign_in")
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

    override suspend fun getOrCreateShopifyCustomerId(firebaseUser: FirebaseUser): Long {
        val savedCustomerId = getSavedShopifyCustomerId(firebaseUser.uid)?.takeIf { it > 0L }
        if (savedCustomerId != null) {
            saveCustomerIdInFireStore(firebaseUser = firebaseUser, customerId = savedCustomerId)
            return savedCustomerId
        }

        val email = firebaseUser.email?.trim().orEmpty()
        if (email.isBlank()) {
            throw IllegalStateException("Google account has no email address")
        }

        val existingCustomerId = findShopifyCustomerIdByEmail(email)
        val customerId = existingCustomerId ?: createShopifyCustomer(firebaseUser, email)
        saveCustomerIdInFireStore(firebaseUser = firebaseUser, customerId = customerId)
        return customerId
    }

    private suspend fun findShopifyCustomerIdByEmail(email: String): Long? {
        val response = authApiService.searchCustomers(query = "email:$email")
        return response.customers.firstOrNull { customer ->
            customer.email.equals(email, ignoreCase = true)
        }?.id ?: response.customers.firstOrNull()?.id
    }

    private suspend fun createShopifyCustomer(firebaseUser: FirebaseUser, email: String): Long {
        val displayName = firebaseUser.displayName.orEmpty().trim()
        val nameParts = displayName.split(" ").filter { it.isNotBlank() }
        val firstName = nameParts.firstOrNull() ?: email.substringBefore('@')
        val lastName = nameParts.drop(1).joinToString(" ")

        val request = CustomerRequest(
            customer = CustomerDto(
                firstName = firstName,
                lastName = lastName,
                email = email,
                verifiedEmail = true,
                sendEmailWelcome = false,
            ),
        )
        return authApiService.createCustomer(request).customer.id
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): RegisterResult {

        try {
            val authResult = mapFirebaseAuthException {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            }
            val user = authResult.user ?: throw IllegalStateException("firebase_user_missing_after_registration")

            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
            mapFirebaseAuthException { user.updateProfile(profileUpdates).await() }
            mapFirebaseAuthException { user.reload().await() }

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

            saveCustomerIdInFireStore(firebaseUser = user, customerId = customerResponse.customer.id)

            return RegisterResult(firebaseUser = user, customerId = customerResponse.customer.id)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // rollback Firebase if Shopify/customer profile creation fails, but keep the original error.
            runCatching { firebaseAuth.currentUser?.delete()?.await() }
            throw e
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun sendEmailVerification() {
        val user = firebaseAuth.currentUser ?: throw IllegalStateException("firebase_no_signed_in_user")
        mapFirebaseAuthException { user.sendEmailVerification().await() }
    }

    override suspend fun isEmailVerified(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        mapFirebaseAuthException { user.reload().await() }
        return user.isEmailVerified
    }


    private suspend fun saveCustomerIdInFireStore(
        firebaseUser: FirebaseUser,
        customerId: Long,
    ) {
        val data = mapOf(
            "customerId" to customerId,
            "email" to firebaseUser.email.orEmpty(),
            "displayName" to firebaseUser.displayName.orEmpty(),
            "photoUrl" to firebaseUser.photoUrl?.toString().orEmpty(),
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        mapFirebaseAuthException { firebaseAuth.sendPasswordResetEmail(email).await() }
    }

    private suspend fun <T> mapFirebaseAuthException(block: suspend () -> T): T = try {
        block()
    } catch (e: FirebaseAuthException) {
        throw FirebaseAuthFailureException(e.errorCode)
    }
}
