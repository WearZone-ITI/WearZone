package com.example.wearzone.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    // Aalaa
    @Serializable
    data object SplashRoute : Route

    @Serializable
    data object OnboardingRoute : Route

    @Serializable
    data object ProfileRoute : Route

    @Serializable
    data object OrderHistoryRoute : Route

    @Serializable
    data class OrderDetailsRoute(val orderId: Long) : Route

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

    @Serializable
    data object CheckoutRoute : Route
    // Hend

    // Ahmed
    @Serializable
    data object LoginRoute : Route
    @Serializable
    data class ProductDetailRoute(val productId: String) : Route


    @Serializable
    data object WishlistRoute : Route

    @Serializable
    data object BrandsRoute : Route

    @Serializable
    data class VendorProductsRoute(val vendorName: String) : Route

    // Ahmed

    // Omar

    @Serializable
    data object MainRoute : Route
    @Serializable
    data object HomeRoute : Route



    @Serializable
    data object CategoriesRoute : Route

    @Serializable
    data object SearchRoute : Route
    @Serializable
    data class ProductListRoute(
        val collectionId: Long,
        val categoryName: String
    )

        // Omar
}
