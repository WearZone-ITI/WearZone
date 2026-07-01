package com.example.wearzone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wearzone.BuildConfig
import com.example.wearzone.presentation.auth.login.LoginScreen
import com.example.wearzone.presentation.auth.register.RegisterScreen
import com.example.wearzone.presentation.cart.CartScreen
import com.example.wearzone.presentation.home.HomeScreen
import com.example.wearzone.presentation.onboarding.OnboardingScreen
import com.example.wearzone.presentation.product.detail.ProductDetailScreen
import com.example.wearzone.presentation.search.SearchScreen
import com.example.wearzone.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
) {

    val navController = rememberNavController()

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
                    // TODO: Navigate to GuestRoute when guest mode is implemented.
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
                navController.navigate(Route.HomeRoute) {
                    popUpTo<Route.RegisterRoute> {
                        inclusive = true
                    }
                }
            }, onNavigateToLogin = {
                navController.navigate(Route.LoginRoute) {
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
                    navController.navigate(Route.LoginRoute) {
                        popUpTo<Route.CartRoute> {
                            inclusive = true
                        }
                    }
                }, 
                onNavigateToCheckout = {/* TODO */}
            )
        }


        // Hend

        // Ahmed
        composable<Route.LoginRoute> {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Route.MainRoute) {
                        popUpTo<Route.LoginRoute> {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate(Route.RegisterRoute) },
            )
        }

        composable<Route.ProductDetailRoute> {
            ProductDetailScreen(
                onNavigateBack = { navController.popBackStack() }
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
                onNavigateToCard = { navController.navigate(Route.CartRoute) },
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
                onNavigateToSettings = {
                    navController.navigate(Route.SettingsRoute)
                },
               onNavigateToProductDetail = { productId ->
                    navController.navigate(Route.ProductDetailRoute(productId))
                },
                onNavigateToCard = {
                    navController.navigate(Route.CartRoute)
                }
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
