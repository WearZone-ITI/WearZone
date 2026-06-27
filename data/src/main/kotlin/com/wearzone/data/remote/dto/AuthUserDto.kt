package com.wearzone.data.remote.dto

data class AuthUserDto(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val phoneNumber: String?,
    val isEmailVerified: Boolean,
)
