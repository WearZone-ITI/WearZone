package com.example.wearzone.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentation.onboarding.OnboardingScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.OnboardingRoute,
        modifier = modifier
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

            // Hend

            // Ahmed
        composable<Route.LoginRoute> {
            val viewModel: com.example.presentation.auth.login.LoginViewModel =
                androidx.hilt.navigation.compose.hiltViewModel()
            com.example.presentation.auth.login.LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {

                },
                onNavigateToRegister = {

                }
            )
        }

            // Ahmed

            // Omer

            // Omer


    }
}