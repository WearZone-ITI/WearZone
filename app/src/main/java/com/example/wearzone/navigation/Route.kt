package com.example.wearzone.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route{

    // Aalaa
    @Serializable
    data object OnboardingRoute : Route

    @Serializable
    data object LoginRoute : Route
    // Aalaa

    // Hend

    // Hend

    // Ahmed
    @Serializable
    data object LoginRoute : Route
    // Ahmed

    // Omer

    // Omer



}
