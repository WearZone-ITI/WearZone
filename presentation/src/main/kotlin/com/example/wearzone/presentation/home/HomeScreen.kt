package com.example.wearzone.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.presentation.R
import com.example.wearzone.presentation.home.components.GreetingSection
import com.example.wearzone.presentation.home.components.HeroBannerSection
import com.example.wearzone.presentation.home.components.NewArrivalsSection
import com.example.wearzone.presentation.home.components.SearchBarSection
import com.example.wearzone.presentation.home.components.TopBar
import com.example.wearzone.presentation.home.components.TopBrandsSection
import com.example.wearzone.presentation.home.components.TrendingSection
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToBrand: (String) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToBrand -> onNavigateToBrand(effect.brandId)
                is HomeUiEffect.NavigateToCategory -> onNavigateToCategory(effect.categoryId)
                is HomeUiEffect.NavigateToProductDetail -> onNavigateToProductDetail(effect.productId)
                is HomeUiEffect.ShowSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar() },
        containerColor = colorResource(id = R.color.top_bar_background)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.Black)
                }
                is HomeUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        item { GreetingSection(state.userName) }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item { SearchBarSection() }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item {
                            HeroBannerSection(
                                product = state.heroProduct,
                                onProductClick = { viewModel.handleIntent(HomeUiIntent.OnProductClicked(it)) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item {
                            TrendingSection(
                                products = state.trendingProducts,
                                onProductClick = { viewModel.handleIntent(HomeUiIntent.OnProductClicked(it)) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item {
                            TopBrandsSection(
                                brands = state.brands,
                                onBrandClick = { viewModel.handleIntent(HomeUiIntent.OnBrandClicked(it)) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item {
                            NewArrivalsSection(
                                products = state.newArrivalProducts,
                                onProductClick = { viewModel.handleIntent(HomeUiIntent.OnProductClicked(it)) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(id = R.string.nav_home)) },
            label = { Text(stringResource(id = R.string.nav_home)) },
            selected = true,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.List, contentDescription = stringResource(id = R.string.nav_categories)) },
            label = { Text(stringResource(id = R.string.nav_categories)) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Search, contentDescription = stringResource(id = R.string.content_desc_search)) },
            label = { Text(stringResource(id = R.string.nav_search)) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = stringResource(id = R.string.nav_wishlist)) },
            label = { Text(stringResource(id = R.string.nav_wishlist)) },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = stringResource(id = R.string.nav_profile)) },
            label = { Text(stringResource(id = R.string.nav_profile)) },
            selected = false,
            onClick = { }
        )
    }
}