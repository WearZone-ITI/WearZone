package com.example.wearzone.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.presentation.R
import com.example.wearzone.presentation.address.form.AddressFormScreen
import com.example.wearzone.presentation.address.list.AddressListScreen
import com.example.wearzone.presentation.categories.CategoriesScreen
import com.example.wearzone.presentation.common.rememberKeyboardVisibility
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.home.HomeScreen
import com.example.wearzone.presentation.order.details.OrderDetailsScreen
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
    onNavigateToRegister: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToProductList: (Long, String) -> Unit,
    onNavigateToBrands: () -> Unit,
    onNavigateToVendorProducts: (String) -> Unit,
    onNavigateToChat: () -> Unit,
) {
    val bottomNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isKeyboardVisible by rememberKeyboardVisibility()

    val shouldHideBottomBar =
        currentDestination?.hasRoute(Route.AddressListRoute::class) == true ||
                currentDestination?.hasRoute(Route.AddressAddRoute::class) == true ||
                currentDestination?.hasRoute(Route.AddressEditRoute::class) == true ||
                currentDestination?.hasRoute(Route.OrderHistoryRoute::class) == true ||
                currentDestination?.hasRoute(Route.OrderDetailsRoute::class) == true
    val shouldShowBottomBar = !shouldHideBottomBar && !isKeyboardVisible

    val navItems = listOf(
        BottomNavItem(
            route = Route.HomeRoute,
            icon = Icons.Default.Home,
            labelRes = R.string.nav_home,
            contentDescriptionRes = R.string.nav_home
        ),
        BottomNavItem(
            route = Route.CategoriesRoute,
            icon = Icons.Outlined.List,
            labelRes = R.string.nav_categories,
            contentDescriptionRes = R.string.nav_categories
        ),
        BottomNavItem(
            route = Route.SearchRoute,
            icon = Icons.Outlined.Search,
            labelRes = R.string.nav_search,
            contentDescriptionRes = R.string.content_desc_search
        ),
        BottomNavItem(
            route = Route.WishlistRoute,
            icon = Icons.Outlined.FavoriteBorder,
            labelRes = R.string.nav_wishlist,
            contentDescriptionRes = R.string.nav_wishlist
        ),
        BottomNavItem(
            route = Route.ProfileRoute,
            icon = Icons.Outlined.Person,
            labelRes = R.string.nav_profile,
            contentDescriptionRes = R.string.nav_profile
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                val selectedIndex = remember(currentDestination) {
                    navItems.indexOfFirst { item ->
                        when (item.labelRes) {
                            R.string.nav_home ->
                                currentDestination?.hasRoute(Route.HomeRoute::class) == true

                            R.string.nav_categories ->
                                currentDestination?.hasRoute(Route.CategoriesRoute::class) == true

                            R.string.nav_search ->
                                currentDestination?.hasRoute(Route.SearchRoute::class) == true

                            R.string.nav_wishlist ->
                                currentDestination?.hasRoute(Route.WishlistRoute::class) == true

                            R.string.nav_profile ->
                                currentDestination?.hasRoute(Route.ProfileRoute::class) == true ||
                                        currentDestination?.hasRoute(Route.OrderHistoryRoute::class) == true

                            else ->
                                currentDestination?.hierarchy?.any {
                                    it.hasRoute(item.route::class)
                                } == true
                        }
                    }.coerceAtLeast(0)
                }

                AnimatedCurvedNavigationBar(
                    items = navItems,
                    selectedIndex = selectedIndex,
                    onItemSelected = { item, isSelected ->
                        when {
                            item.route == Route.ProfileRoute -> {
                                if (currentDestination?.hasRoute(Route.ProfileRoute::class) != true) {
                                    val returnedToProfile = bottomNavController.popBackStack(
                                        route = Route.ProfileRoute,
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
                                    bottomNavController.popBackStack(
                                        route = Route.HomeRoute,
                                        inclusive = false
                                    )
                                } else {
                                    bottomNavController.popBackStack(
                                        route = Route.HomeRoute,
                                        inclusive = false
                                    )
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
                    }
                )
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
                    onNavigateToProductDetail = { productId ->
                        onNavigateToProductDetail(productId)
                    },
                    onNavigateToCategory = {
                        bottomNavController.navigate(Route.CategoriesRoute)
                    },
                    onNavigateToBrand = { brandName ->
                        onNavigateToVendorProducts(brandName)
                    },
                    onNavigateToBrands = {
                        onNavigateToBrands()
                    },
                    onNavigateToSearch = {
                        bottomNavController.navigate(Route.SearchRoute)
                    },
                    onNavigateToChat = onNavigateToChat,
                    onShowSnackbar = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                    onNavigateToCart = {
                        onNavigateToCart()
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
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
                    onNavigateBack = {
                        val popped = bottomNavController.popBackStack()
                        if (!popped) {
                            bottomNavController.navigate(Route.HomeRoute) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToProductDetail = { productId ->
                        onNavigateToProductDetail(productId)
                    },
                    onNavigateToCart = {
                        onNavigateToCart()
                    }
                )
            }

            composable<Route.ProfileRoute> {
                ProfileScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToWishlist = {
                        bottomNavController.navigate(Route.WishlistRoute)
                    },
                    onNavigateToOrders = {
                        bottomNavController.navigate(Route.OrderHistoryRoute) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCart = {
                        onNavigateToCart()
                    },
                    onNavigateToSavedAddresses = {
                        bottomNavController.navigate(Route.AddressListRoute) {
                            launchSingleTop = true
                        }
                    },
                    onContinueBrowsing = {
                        bottomNavController.navigate(Route.HomeRoute) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable<Route.OrderHistoryRoute> {
                OrderHistoryScreen(
                    onNavigateBack = {
                        bottomNavController.popBackStack()
                    },
                    onNavigateToDetails = { orderId ->
                        bottomNavController.navigate(Route.OrderDetailsRoute(orderId))
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                )
            }

            composable<Route.OrderDetailsRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.OrderDetailsRoute>()

                OrderDetailsScreen(
                    orderId = route.orderId,
                    onNavigateBack = {
                        bottomNavController.popBackStack()
                    },
                    onContinueShopping = {
                        bottomNavController.navigate(Route.HomeRoute) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                )
            }

            composable<Route.AddressListRoute> { backStackEntry ->
                val refreshAfterChange by backStackEntry.savedStateHandle
                    .getStateFlow(ADDRESS_CHANGED_KEY, false)
                    .collectAsStateWithLifecycle()

                AddressListScreen(
                    onNavigateBack = {
                        bottomNavController.popBackStack()
                    },
                    onNavigateToAddAddress = {
                        bottomNavController.navigate(Route.AddressAddRoute)
                    },
                    onNavigateToEditAddress = { addressId ->
                        bottomNavController.navigate(Route.AddressEditRoute(addressId))
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    refreshAfterChange = refreshAfterChange,
                    onRefreshAfterChangeConsumed = {
                        backStackEntry.savedStateHandle[ADDRESS_CHANGED_KEY] = false
                    },
                )
            }

            composable<Route.AddressAddRoute> {
                AddressFormScreen(
                    addressId = null,
                    onNavigateBack = {
                        bottomNavController.popBackStack()
                    },
                    onAddressSaved = {
                        bottomNavController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(ADDRESS_CHANGED_KEY, true)
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                )
            }

            composable<Route.AddressEditRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.AddressEditRoute>()

                AddressFormScreen(
                    addressId = route.addressId,
                    onNavigateBack = {
                        bottomNavController.popBackStack()
                    },
                    onAddressSaved = {
                        bottomNavController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(ADDRESS_CHANGED_KEY, true)
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                )
            }

            composable<Route.WishlistRoute> {
                WishlistScreen(
                    onNavigateToProductDetail = { productId ->
                        onNavigateToProductDetail(productId)
                    },
                    onShowSnackbar = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                    onNavigateToCart = onNavigateToCart,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    onContinueBrowsing = {
                        bottomNavController.navigate(Route.HomeRoute) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun AnimatedCurvedNavigationBar(
    items: List<BottomNavItem<out Any>>,
    selectedIndex: Int,
    onItemSelected: (BottomNavItem<out Any>, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                clip = false
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onItemSelected(item, isSelected)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(id = item.contentDescriptionRes),
                            modifier = Modifier.size(
                                if (isSelected) 23.dp else 22.dp
                            ),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = stringResource(id = item.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}