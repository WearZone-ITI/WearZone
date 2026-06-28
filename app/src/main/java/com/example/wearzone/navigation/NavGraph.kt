package com.example.wearzone.navigation

import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.presentation.auth.login.LoginPlaceholderScreen
import com.example.presentation.onboarding.OnboardingScreen
import com.example.presentation.onboarding.OnboardingViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
){

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.SplashRoute,
        modifier = modifier
    ) {
        // Aalaa
        composable<Route.SplashRoute> {
            val viewModel: OnboardingViewModel = hiltViewModel()
            val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()

            LaunchedEffect(hasCompletedOnboarding) {
                // TODO: Add auth-based routing after the real auth feature exists.
                when (hasCompletedOnboarding) {
                    false -> navController.navigate(Route.OnboardingRoute) {
                        popUpTo<Route.SplashRoute> { inclusive = true }
                    }
                    true -> navController.navigate(Route.LoginRoute) {
                        popUpTo<Route.SplashRoute> { inclusive = true }
                    }
                    null -> Unit
                }
            }

            Text(text = "Loading")
        }

        composable<Route.OnboardingRoute> {
            val context = LocalContext.current
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.LoginRoute) {
                        popUpTo<Route.OnboardingRoute> { inclusive = true }
                    }
                },
                onShowMessage = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
            )
        }

        composable<Route.LoginRoute> {
            LoginPlaceholderScreen()
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
