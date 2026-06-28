package com.example.wearzone.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route{

    // Aalaa
    @Serializable
    data object SplashRoute : Route

    @Serializable
    data object OnboardingRoute : Route

    @Serializable
    data object LoginRoute : Route
    // Aalaa

    // Hend

    // Hend

    // Ahmed

    // Ahmed

    // Omer

    // Omer



}
