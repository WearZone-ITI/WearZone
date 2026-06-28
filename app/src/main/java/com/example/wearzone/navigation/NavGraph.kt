package com.example.wearzone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentation.onboarding.OnboardingScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
){

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
                    // TODO: Navigate to LoginRoute when auth flow is implemented.
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

        // Ahmed

        // Omer

        // Omer
    }
  
}
