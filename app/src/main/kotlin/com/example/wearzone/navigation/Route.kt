package com.example.wearzone.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    // Aalaa
    @Serializable
    data object OnboardingRoute : Route
=======

    @Serializable
    data object ProfileRoute : Route

    @Serializable
    data object SettingsRoute : Route

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
    data object SearchRoute : Route
    // Omar
}
