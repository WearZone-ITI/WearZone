package com.example.wearzone.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    // Aalaa
    @Serializable
    data object OnboardingRoute : Route

    @Serializable
    data object ProfileRoute : Route

    @Serializable
    data object SettingsRoute : Route

    @Serializable
    data object AddressListRoute : Route

    @Serializable
    data object AddressAddRoute : Route

    @Serializable
    data class AddressEditRoute(val addressId: Long) : Route

    // Aalaa

    // Hend
    @Serializable
    data object RegisterRoute : Route
    @Serializable
    data object CartRoute  : Route
    // Hend

    // Ahmed
    @Serializable
    data object LoginRoute : Route
    // Ahmed

    // Omar

    @Serializable
    data object MainRoute : Route
    @Serializable
    data object HomeRoute : Route

    @Serializable
    data class ProductDetailRoute(val productId: String) : Route

    @Serializable
    data object SearchRoute : Route
    
    @Serializable
    data object WishlistRoute : Route
    // Omar
}
