package com.example.wearzone.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.home.HomeScreen
import com.example.wearzone.presentation.profile.ProfileScreen
import com.example.wearzone.presentation.search.SearchScreen
import kotlinx.coroutines.launch

data class BottomNavItem<T : Any>(
    val route: T,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelRes: Int,
    val contentDescriptionRes: Int
)

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProductDetail: (Long) -> Unit,
) {
    val bottomNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val navItems = listOf(
        BottomNavItem(Route.HomeRoute, Icons.Default.Home, R.string.nav_home, R.string.nav_home),
        BottomNavItem(Route.HomeRoute, Icons.Outlined.List, R.string.nav_categories, R.string.nav_categories),
        BottomNavItem(Route.SearchRoute, Icons.Outlined.Search, R.string.nav_search, R.string.content_desc_search),
        BottomNavItem(Route.WishlistRoute, Icons.Outlined.FavoriteBorder, R.string.nav_wishlist, R.string.nav_wishlist),
        BottomNavItem(Route.ProfileRoute, Icons.Outlined.Person, R.string.nav_profile, R.string.nav_profile)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            val itemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppTheme.colors.selected,
                selectedTextColor = AppTheme.colors.selected,
                indicatorColor = AppTheme.colors.surfaceVariant,
                unselectedIconColor = AppTheme.colors.textSecondary,
                unselectedTextColor = AppTheme.colors.textSecondary,
            )

            NavigationBar(
                containerColor = AppTheme.colors.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    val isRouteMatch = currentDestination?.hierarchy?.any {
                        it.hasRoute(item.route::class)
                    } == true

                    val isSelected = when (item.labelRes) {
                        R.string.nav_home -> currentDestination?.hasRoute(Route.HomeRoute::class) == true
                        R.string.nav_categories -> false
                        R.string.nav_wishlist -> currentDestination?.hasRoute(Route.WishlistRoute::class) == true
                        R.string.nav_search -> currentDestination?.hasRoute(Route.SearchRoute::class) == true
                        else -> isRouteMatch
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        label = {
                            Text(
                                text = stringResource(id = item.labelRes),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(id = item.contentDescriptionRes),
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        onClick = {
                            if (item.labelRes != R.string.nav_categories) {

                                if (item.route == Route.HomeRoute && currentDestination?.hasRoute(Route.SearchRoute::class) == true) {
                                    bottomNavController.popBackStack(Route.HomeRoute, inclusive = false)
                                }
                                else if (item.route != Route.HomeRoute && currentDestination?.hasRoute(Route.SearchRoute::class) == true) {
                                    bottomNavController.popBackStack(Route.HomeRoute, inclusive = false)

                                    bottomNavController.navigate(item.route) {
                                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                else if (!isSelected) {
                                    bottomNavController.navigate(item.route) {
                                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                        colors = itemColors,
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Route.HomeRoute,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable<Route.HomeRoute> {
                HomeScreen(
                    onNavigateToProductDetail = { productId -> onNavigateToProductDetail(productId.toLong()) },
                    onNavigateToCategory = { },
                    onNavigateToBrand = { },
                    onNavigateToSearch = { bottomNavController.navigate(Route.SearchRoute) },
                    onShowSnackbar = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                    onNavigateToProfile = { bottomNavController.navigate(Route.ProfileRoute) }
                )
            }

            composable<Route.SearchRoute> {
                SearchScreen(
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onNavigateToProductDetail = { productId -> onNavigateToProductDetail(productId.toLong()) }
                )
            }

            composable<Route.ProfileRoute> {
                ProfileScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToWishlist = { 
                        bottomNavController.navigate(Route.WishlistRoute)
                    },
                    onNavigateToOrders = { },
                    onNavigateToSavedAddresses = { },
                    onNavigateToHome = {
                        bottomNavController.navigate(Route.HomeRoute) {
                            popUpTo<Route.HomeRoute> { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.WishlistRoute> {
                com.example.wearzone.presentation.wishlist.WishlistScreen(
                    onNavigateToProductDetail = { productId -> onNavigateToProductDetail(productId.toLong()) },
                    onShowSnackbar = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }
}
