package com.example.wearzone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.wearzone.BuildConfig
import com.example.wearzone.presentation.address.form.AddressFormScreen
import com.example.wearzone.presentation.address.list.AddressListScreen
import com.example.wearzone.presentation.auth.emailverification.EmailVerificationScreen
import com.example.wearzone.presentation.auth.login.LoginScreen
import com.example.wearzone.presentation.auth.register.RegisterScreen
import com.example.wearzone.presentation.cart.CartScreen
import com.example.wearzone.presentation.checkout.CheckoutScreen
import com.example.wearzone.presentation.home.HomeScreen
import com.example.wearzone.presentation.onboarding.OnboardingScreen
import com.example.wearzone.presentation.order.details.OrderDetailsScreen
import com.example.wearzone.presentation.order.history.OrderHistoryScreen
import com.example.wearzone.presentation.product.detail.ProductDetailScreen
import com.example.wearzone.presentation.product.list.ProductListScreen
import com.example.wearzone.presentation.search.SearchScreen
import com.example.wearzone.presentation.settings.SettingsScreen
import com.example.wearzone.presentation.brands.BrandsScreen
import com.example.wearzone.presentation.vendor_products.VendorProductsScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
) {

    val navController = rememberNavController()
    var pendingProtectedRoute by rememberSaveable { mutableStateOf<Route?>(null) }

    fun navigateToPendingOrMain(sourceRoute: Route) {
        val destination = pendingProtectedRoute ?: Route.MainRoute
        pendingProtectedRoute = null
        navController.navigate(destination) {
            popUpTo(sourceRoute) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateToLoginForProtectedRoute(route: Route) {
        pendingProtectedRoute = route
        navController.navigate(Route.LoginRoute)
    }

    fun navigateToRegisterForProtectedRoute(route: Route) {
        pendingProtectedRoute = route
        navController.navigate(Route.RegisterRoute)
    }

    NavHost(
        navController = navController,
        startDestination = Route.OnboardingRoute,
        modifier = modifier,
    ) {
        // Aalaa
        composable<Route.OnboardingRoute> {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.LoginRoute) {
                        popUpTo<Route.OnboardingRoute> {
                            inclusive = true
                        }
                    }
                },
                onNavigateToGuest = {
                    navController.navigate(Route.MainRoute) {
                        popUpTo<Route.OnboardingRoute> {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable<Route.SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                appVersion = BuildConfig.VERSION_NAME,
            )
        }



        // Aalaa

        // Hend
        composable<Route.RegisterRoute> {
            RegisterScreen(onNavigateToHome = {
                navigateToPendingOrMain(Route.RegisterRoute)
            }, onNavigateToLogin = {
                navController.navigate(Route.LoginRoute) {
                    popUpTo<Route.RegisterRoute> {
                        inclusive = true
                    }
                }
            }, onNavigateToEmailVerification = {
                navController.navigate(Route.EmailVerificationRoute) {
                    popUpTo<Route.RegisterRoute> {
                        inclusive = true
                    }
                }
            })
        }


        composable<Route.CartRoute> {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.CheckoutRoute)
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.CheckoutRoute)
                }, 
                onNavigateToCheckout = { navController.navigate(Route.CheckoutRoute) }
            )
        }

        composable<Route.CheckoutRoute> { backStackEntry ->
            val refreshAfterAddressChange by backStackEntry.savedStateHandle
                .getStateFlow(NavigationKeys.ADDRESS_CHANGED, false)
                .collectAsStateWithLifecycle()

            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOrderHistory = {
                    navController.navigate(Route.OrderHistoryRoute) {
                        popUpTo<Route.CartRoute> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.CheckoutRoute)
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.CheckoutRoute)
                },
                onNavigateToAddressList = {
                    navController.navigate(Route.AddressListRoute)
                },
                onNavigateToAddAddress = {
                    navController.navigate(Route.AddressAddRoute)
                },
                refreshAfterAddressChange = refreshAfterAddressChange,
                onRefreshAfterAddressChangeConsumed = {
                    backStackEntry.savedStateHandle[NavigationKeys.ADDRESS_CHANGED] = false
                },
            )
        }

        composable<Route.AddressListRoute> { backStackEntry ->
            val refreshAfterChange by backStackEntry.savedStateHandle
                .getStateFlow(NavigationKeys.ADDRESS_CHANGED, false)
                .collectAsStateWithLifecycle()

            AddressListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddAddress = { navController.navigate(Route.AddressAddRoute) },
                onNavigateToEditAddress = { addressId ->
                    navController.navigate(Route.AddressEditRoute(addressId))
                },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.AddressListRoute)
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.AddressListRoute)
                },
                refreshAfterChange = refreshAfterChange,
                onRefreshAfterChangeConsumed = {
                    backStackEntry.savedStateHandle[NavigationKeys.ADDRESS_CHANGED] = false
                },
            )
        }

        composable<Route.AddressAddRoute> {
            AddressFormScreen(
                addressId = null,
                onNavigateBack = { navController.popBackStack() },
                onAddressSaved = {
                    navController.markAddressChangedForCheckout()
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(NavigationKeys.ADDRESS_CHANGED, true)
                },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.AddressAddRoute)
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.AddressAddRoute)
                },
            )
        }

        composable<Route.AddressEditRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.AddressEditRoute>()
            AddressFormScreen(
                addressId = route.addressId,
                onNavigateBack = { navController.popBackStack() },
                onAddressSaved = {
                    navController.markAddressChangedForCheckout()
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(NavigationKeys.ADDRESS_CHANGED, true)
                },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.AddressEditRoute(route.addressId))
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.AddressEditRoute(route.addressId))
                },
            )
        }

        composable<Route.OrderHistoryRoute> {
            OrderHistoryScreen(
                onNavigateBack = {
                    val returnedToPrevious = navController.popBackStack()
                    if (!returnedToPrevious) {
                        navController.navigate(Route.MainRoute) {
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateToDetails = { orderId ->
                    navController.navigate(Route.OrderDetailsRoute(orderId))
                },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.OrderHistoryRoute)
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.OrderHistoryRoute)
                },
            )
        }

        composable<Route.OrderDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.OrderDetailsRoute>()
            OrderDetailsScreen(
                orderId = route.orderId,
                onNavigateBack = { navController.popBackStack() },
                onContinueShopping = {
                    navController.navigate(Route.MainRoute) {
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.OrderDetailsRoute(route.orderId))
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.OrderDetailsRoute(route.orderId))
                },
            )
        }


        // Hend

        // Ahmed
        composable<Route.LoginRoute> {
            LoginScreen(
                onNavigateToHome = {
                    navigateToPendingOrMain(Route.LoginRoute)
                },
                onNavigateToRegister = { navController.navigate(Route.RegisterRoute) },
                onNavigateToEmailVerification = {
                    navController.navigate(Route.EmailVerificationRoute) {
                        popUpTo<Route.LoginRoute> {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable<Route.ProductDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.ProductDetailRoute>()
            ProductDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.ProductDetailRoute(route.productId))
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.ProductDetailRoute(route.productId))
                },
            )
        }

        // Ahmed
        // Ahmed

        // Omar
        composable<Route.HomeRoute> {
            HomeScreen(
                onNavigateToProductDetail = { productId -> },
                onNavigateToCategory = { categoryId -> },
                onNavigateToBrand = { brandId -> },
                onNavigateToBrands = {},
                onNavigateToCart = { navController.navigate(Route.CartRoute) },
                onNavigateToLogin = { navController.navigate(Route.LoginRoute) },
                onNavigateToRegister = { navController.navigate(Route.RegisterRoute) },
                onShowSnackbar = { message -> },
                onNavigateToSearch = {}
            )
        }
        composable<Route.MainRoute> {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.LoginRoute) {
                        popUpTo<Route.MainRoute> {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Route.RegisterRoute)
                },
                onNavigateToSettings = {
                    navController.navigate(Route.SettingsRoute)
                },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Route.ProductDetailRoute(productId))
                },
                onNavigateToCart = {
                    navController.navigate(Route.CartRoute)
                },
                onNavigateToProductList = { collectionId, categoryName ->
                    navController.navigate(
                        Route.ProductListRoute(
                            collectionId = collectionId,
                            categoryName = categoryName
                        )
                    )
                },
                onNavigateToBrands = {
                    navController.navigate(Route.BrandsRoute)
                },
                onNavigateToVendorProducts = { vendorName ->
                    navController.navigate(Route.VendorProductsRoute(vendorName))
                }
            )
        }
        composable<Route.ProductListRoute> { backStackEntry ->

            val route = backStackEntry.toRoute<Route.ProductListRoute>()

            ProductListScreen(
                collectionId = route.collectionId,
                categoryName = route.categoryName,
                onNavigateBack = {
                    navController.popBackStack()
                },

                onNavigateToProductDetail = { productId ->
                    navController.navigate(
                        Route.ProductDetailRoute(productId)
                    )
                },
                onNavigateToCart = { navController.navigate(Route.CartRoute) }
            )
        }
        composable<Route.EmailVerificationRoute> {
            EmailVerificationScreen(
                onNavigateToHome = {
                    navigateToPendingOrMain(Route.EmailVerificationRoute)
                },
                onNavigateToLogin = {
                    navController.navigate(Route.LoginRoute) {
                        popUpTo<Route.EmailVerificationRoute> {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<Route.BrandsRoute> {
            BrandsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVendorProducts = { vendorName ->
                    navController.navigate(Route.VendorProductsRoute(vendorName))
                }
            )
        }

        composable<Route.VendorProductsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.VendorProductsRoute>()
            VendorProductsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Route.ProductDetailRoute(productId))
                },
                onNavigateToLogin = {
                    navigateToLoginForProtectedRoute(Route.VendorProductsRoute(route.vendorName))
                },
                onNavigateToRegister = {
                    navigateToRegisterForProtectedRoute(Route.VendorProductsRoute(route.vendorName))
                },
            )
        }

        composable<Route.SearchRoute> {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Route.ProductDetailRoute(productId))
                },
                onNavigateToCart = { navController.navigate(Route.CartRoute) }
            )
        }
        // Omar
    }
}

private fun NavHostController.markAddressChangedForCheckout() {
    runCatching {
        getBackStackEntry<Route.CheckoutRoute>()
            .savedStateHandle[NavigationKeys.ADDRESS_CHANGED] = true
    }
}
