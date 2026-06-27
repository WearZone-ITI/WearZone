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
    @Serializable
    data object LoginRoute : Route

    @Serializable
    data object RegisterRoute : Route
    // Ahmed

    // Omer

    // Omer



}