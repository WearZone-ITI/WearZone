package com.example.wearzone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wearzone.presentation.auth.login.LoginScreen
import com.example.wearzone.presentation.auth.register.RegisterScreen
import com.example.wearzone.presentation.home.HomeScreen
import com.example.wearzone.presentation.onboarding.OnboardingScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController, startDestination = Route.OnboardingRoute, modifier = modifier
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
        // Aalaa

        // Hend
        composable<Route.RegisterRoute> {
           RegisterScreen(
               onNavigateToHome = {
                    navController.navigate(Route.HomeRoute) {
                        popUpTo<Route.RegisterRoute> {
                            inclusive = true
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Route.LoginRoute) {
                        popUpTo<Route.RegisterRoute> {
                            inclusive = true
                        }
                    }
                })
        }
        // Hend

        // Ahmed
        composable<Route.LoginRoute> {
            LoginScreen(onNavigateToHome = {
                navController.navigate(Route.HomeRoute) {
                    popUpTo<Route.LoginRoute> {
                        inclusive = true
                    }
                }
            },
                onNavigateToRegister = { navController.navigate(Route.RegisterRoute) }
            )
        }
        // Ahmed

        // Omar
        composable<Route.HomeRoute> {
            HomeScreen(
                onNavigateToProductDetail = { productId -> },
                onNavigateToCategory = { categoryId -> },
                onNavigateToBrand = { brandId -> },
                onShowSnackbar = { message -> })
        }
        // Omar
    }
}