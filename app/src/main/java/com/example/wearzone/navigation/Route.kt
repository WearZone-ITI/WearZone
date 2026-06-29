package com.example.wearzone.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route{

    // Aalaa

    @Serializable
    data object OnboardingRoute : Route

    // Aalaa

    // Hend
    @Serializable
    data object RegisterRoute : Route
    // Hend

    // Ahmed
    @Serializable
    data object LoginRoute : Route
    // Ahmed

    // Omar
    @Serializable
    data object HomeRoute : Route

    @Serializable
    data class ProductDetailRoute(val productId: Long) : Route
    // Omar



}
