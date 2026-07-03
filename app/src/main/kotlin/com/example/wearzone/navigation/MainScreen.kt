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
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.navigation.toRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.categories.CategoriesScreen
import com.example.wearzone.presentation.address.form.AddressFormScreen
import com.example.wearzone.presentation.address.list.AddressListScreen
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.home.HomeScreen
import com.example.wearzone.presentation.order.history.OrderHistoryScreen
import com.example.wearzone.presentation.profile.ProfileScreen
import com.example.wearzone.presentation.search.SearchScreen
import com.example.wearzone.presentation.wishlist.WishlistScreen
import kotlinx.coroutines.launch

data class BottomNavItem<T : Any>(
    val route: T,
    val icon: ImageVector,
    val labelRes: Int,
    val contentDescriptionRes: Int
)

private const val ADDRESS_CHANGED_KEY = NavigationKeys.ADDRESS_CHANGED

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToProductList: (Long, String) -> Unit,
    onNavigateToBrands: () -> Unit,
    onNavigateToVendorProducts: (String) -> Unit,
) {
    val bottomNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val navItems = listOf(
        BottomNavItem(Route.HomeRoute, Icons.Default.Home, R.string.nav_home, R.string.nav_home),
        BottomNavItem(Route.CategoriesRoute, Icons.Outlined.List, R.string.nav_categories, R.string.nav_categories),
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
                containerColor = AppTheme.colors.surface, tonalElevation = 8.dp
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    val isRouteMatch = currentDestination?.hierarchy?.any {
                        it.hasRoute(item.route::class)
                    } == true

                    val isSelected = when (item.labelRes) {
                        R.string.nav_home -> currentDestination?.hasRoute(Route.HomeRoute::class) == true
                        R.string.nav_categories -> currentDestination?.hasRoute(Route.CategoriesRoute::class) == true
                        R.string.nav_wishlist -> currentDestination?.hasRoute(Route.WishlistRoute::class) == true
                        R.string.nav_search -> currentDestination?.hasRoute(Route.SearchRoute::class) == true
                        R.string.nav_profile -> currentDestination?.hasRoute(Route.ProfileRoute::class) == true ||
                            currentDestination?.hasRoute(Route.OrderHistoryRoute::class) == true
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
                            when {
                                item.route == Route.ProfileRoute -> {
                                    if (currentDestination?.hasRoute(Route.ProfileRoute::class) != true) {
                                        val returnedToProfile = bottomNavController.popBackStack(
                                            Route.ProfileRoute,
                                            inclusive = false,
                                        )
                                        if (!returnedToProfile) {
                                            bottomNavController.navigate(Route.ProfileRoute) {
                                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                }

                                currentDestination?.hasRoute(Route.SearchRoute::class) == true -> {
                                    if (item.route == Route.HomeRoute) {
                                        bottomNavController.popBackStack(Route.HomeRoute, inclusive = false)
                                    } else {
                                        bottomNavController.popBackStack(Route.HomeRoute, inclusive = false)
                                        bottomNavController.navigate(item.route) {
                                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }

                                !isSelected -> {
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
            }) { paddingValues ->
            NavHost(
                navController = bottomNavController,
                startDestination = Route.HomeRoute,
                modifier = Modifier.padding(paddingValues),
            ) {
                composable<Route.HomeRoute> {
                    HomeScreen(
                    onNavigateToProductDetail = { productId -> onNavigateToProductDetail(productId) },
                    onNavigateToCategory = { bottomNavController.navigate(Route.CategoriesRoute) },
                    onNavigateToBrand = { brandName -> onNavigateToVendorProducts(brandName) },
                    onNavigateToBrands = { onNavigateToBrands() },
                    onNavigateToSearch = { bottomNavController.navigate(Route.SearchRoute) },
                    onShowSnackbar = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                    onNavigateToCart = { onNavigateToCart() },
                    )
                }
                composable<Route.CategoriesRoute> {
                    CategoriesScreen(
                        navigateToCart = onNavigateToCart,
                        onNavigateToProductList = { collectionId, categoryName ->
                            onNavigateToProductList(collectionId, categoryName)
                        }
                    )
                }
                composable<Route.SearchRoute> {
                    SearchScreen(
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onNavigateToProductDetail = { productId -> onNavigateToProductDetail(productId) },
                        onNavigateToCart = {onNavigateToCart()}
                    )
                }

                composable<Route.ProfileRoute> {
                    ProfileScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToWishlist = {
                            bottomNavController.navigate(Route.WishlistRoute)
                        },
                        onNavigateToOrders = {
                            bottomNavController.navigate(Route.OrderHistoryRoute) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCart = { onNavigateToCart() },
                        onNavigateToSavedAddresses = {
                            bottomNavController.navigate(Route.AddressListRoute) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Route.OrderHistoryRoute> {
                    OrderHistoryScreen(
                        onNavigateBack = { bottomNavController.popBackStack() },
                    )
                }

                composable<Route.AddressListRoute> { backStackEntry ->
                    val refreshAfterChange by backStackEntry.savedStateHandle
                        .getStateFlow(ADDRESS_CHANGED_KEY, false)
                        .collectAsStateWithLifecycle()

                    AddressListScreen(
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onNavigateToAddAddress = { bottomNavController.navigate(Route.AddressAddRoute) },
                        onNavigateToEditAddress = { addressId ->
                            bottomNavController.navigate(Route.AddressEditRoute(addressId))
                        },
                        refreshAfterChange = refreshAfterChange,
                        onRefreshAfterChangeConsumed = {
                            backStackEntry.savedStateHandle[ADDRESS_CHANGED_KEY] = false
                        },
                    )
                }

                composable<Route.AddressAddRoute> {
                    AddressFormScreen(
                        addressId = null,
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onAddressSaved = {
                            bottomNavController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(ADDRESS_CHANGED_KEY, true)
                        },
                    )
                }

                composable<Route.AddressEditRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<Route.AddressEditRoute>()
                    AddressFormScreen(
                        addressId = route.addressId,
                        onNavigateBack = { bottomNavController.popBackStack() },
                        onAddressSaved = {
                            bottomNavController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(ADDRESS_CHANGED_KEY, true)
                        },
                    )
                }

                composable<Route.WishlistRoute> {
                    WishlistScreen(
                        onNavigateToProductDetail = { productId -> onNavigateToProductDetail(productId) },
                        onShowSnackbar = { message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        },
                        onNavigateToCart = onNavigateToCart
                    )
                }
            }
        }
        }
