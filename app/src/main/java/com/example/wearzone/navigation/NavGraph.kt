package com.example.wearzone.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wearzone.presentation.home.HomeScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.SplashRoute,
        modifier = modifier
    ) {
        // Aalaa
        composable<Route.SplashRoute> {
            Text(text = "Splash Screen")
        }
        // Aalaa

        // Hend

        // Hend

        // Ahmed

        // Ahmed

        // Omar
        // Omar
        composable<Route.HomeRoute> {
            HomeScreen(
                onNavigateToProductDetail = { productId -> },
                onNavigateToCategory = { categoryId -> },
                onNavigateToBrand = { brandId -> },
                onShowSnackbar = { message -> }
            )
        }
        // Omar
    }
}