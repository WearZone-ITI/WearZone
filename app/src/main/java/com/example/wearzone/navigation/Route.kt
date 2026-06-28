package com.example.wearzone.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route{

    // Aalaa
    @Serializable
    data object SplashRoute : Route
    // Aalaa

    // Hend

    // Hend

    // Ahmed

    // Ahmed

    // Omar
    @Serializable
    data object HomeRoute : Route
    // Omar



}