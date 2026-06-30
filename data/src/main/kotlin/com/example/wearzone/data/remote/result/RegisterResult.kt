package com.example.wearzone.data.remote.result

import com.google.firebase.auth.FirebaseUser

data class RegisterResult(
    val firebaseUser: FirebaseUser,
    val customerId: Long
)