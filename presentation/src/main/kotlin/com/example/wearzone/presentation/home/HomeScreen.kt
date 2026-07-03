package com.example.wearzone.presentation.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.GreetingSection
import com.example.wearzone.presentation.common.PromoAdsCarousel
import com.example.wearzone.presentation.home.components.HeroBannerSection
import com.example.wearzone.presentation.home.components.NewArrivalsSection
import com.example.wearzone.presentation.home.components.SearchBarSection
import com.example.wearzone.presentation.common.TopBar
import com.example.wearzone.presentation.home.components.TopBrandsSection
import com.example.wearzone.presentation.home.components.TrendingSection
import com.example.wearzone.presentation.wishlist.components.RemoveFavoriteDialog
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToBrand: (String) -> Unit,
    onNavigateToBrands: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart : ()->Unit,
    onShowSnackbar: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToBrand -> onNavigateToBrand(effect.brandId)
                is HomeUiEffect.NavigateToCategory -> onNavigateToCategory(effect.categoryId)
                is HomeUiEffect.NavigateToProductDetail -> onNavigateToProductDetail(effect.productId)
                is HomeUiEffect.NavigateToCart -> onNavigateToCart()
                is HomeUiEffect.ShowSnackbar -> onShowSnackbar(context.resources.getString(effect.messageResId))
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                cartItemCount = if (uiState is HomeUiState.Success) (uiState as HomeUiState.Success).cartItemCount else 0,
                onAddToCartClick = { viewModel.handleIntent(HomeUiIntent.OnCartClicked) }
            )
        },
        containerColor = AppTheme.colors.background,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AppTheme.colors.selected,
                    )
                }
                is HomeUiState.Error -> {
                    Text(
                        text = state.message,
                        color = AppTheme.colors.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Success -> {
                    if (state.productToRemove != null) {
                        RemoveFavoriteDialog(
                            onConfirm = { viewModel.handleIntent(HomeUiIntent.OnConfirmRemove) },
                            onDismiss = { viewModel.handleIntent(HomeUiIntent.OnCancelRemove) }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    ) {
                        item { GreetingSection(state.userName) }
                        item { Spacer(modifier = Modifier.height(6.dp)) }


                        item(key = "promo_ads") {
                            PromoAdsCarousel(
                                products = state.promoAds,
                                onProductClick = { product ->
                                    viewModel.handleIntent(HomeUiIntent.OnProductClicked(product.id))
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                        item { SearchBarSection(onClick = onNavigateToSearch) }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item {
                            HeroBannerSection(
                                product = state.heroProduct,
                                onProductClick = { viewModel.handleIntent(HomeUiIntent.OnProductClicked(it)) },
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item {
                            TrendingSection(
                                products = state.trendingProducts,
                                onProductClick = { viewModel.handleIntent(HomeUiIntent.OnProductClicked(it)) },
                                onAddToCartClick = { viewModel.handleIntent(HomeUiIntent.OnAddToCartClicked(it)) },
                                onFavoriteClick = { viewModel.handleIntent(HomeUiIntent.OnFavoriteClicked(it)) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }



                        item {
                            TopBrandsSection(
                                brands = state.brands,
                                onBrandClick = { viewModel.handleIntent(HomeUiIntent.OnBrandClicked(it)) },
                                onSeeAllClick = onNavigateToBrands
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                        item {
                            NewArrivalsSection(
                                products = state.newArrivalProducts,
                                onProductClick = { viewModel.handleIntent(HomeUiIntent.OnProductClicked(it)) },
                                onAddToCartClick = { viewModel.handleIntent(HomeUiIntent.OnAddToCartClicked(it)) },
                                onFavoriteClick = { viewModel.handleIntent(HomeUiIntent.OnFavoriteClicked(it)) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }
    }
}