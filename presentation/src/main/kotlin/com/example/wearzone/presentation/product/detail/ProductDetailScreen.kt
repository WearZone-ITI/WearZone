package com.example.wearzone.presentation.product.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wearzone.presentation.common.theme.AppColors
import com.example.presentation.R
import com.example.wearzone.presentation.product.detail.components.ImageCarousel
import com.example.wearzone.presentation.product.detail.components.SizeSelector
import com.example.wearzone.presentation.product.detail.components.StarRatingRow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val authRequiredMessage = stringResource(id = R.string.product_detail_auth_required)

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ProductDetailUiEffect.ShowToast -> snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                is ProductDetailUiEffect.ShowAuthRequiredError -> snackbarHostState.showSnackbar(authRequiredMessage)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (uiState is ProductDetailUiState.Success) {
                ProductDetailBottomBar(
                    onAddToCartClick = { viewModel.handleIntent(ProductDetailUiIntent.AddToCart) }
                )
            }
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is ProductDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                }
                is ProductDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WifiOff,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 16.dp).size(120.dp),
                            tint = AppColors.Error
                        )
                        Text(
                            text = stringResource(id = state.messageRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.handleIntent(ProductDetailUiIntent.Retry) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                contentColor = AppColors.OnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(0.5f).height(48.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.product_detail_retry),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
                is ProductDetailUiState.Success -> {
                    ProductDetailContent(
                        state = state,
                        onNavigateBack = onNavigateBack,
                        onIntent = { viewModel.handleIntent(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    state: ProductDetailUiState.Success,
    onNavigateBack: () -> Unit,
    onIntent: (ProductDetailUiIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // ~50% of screen height
            ) {
                ImageCarousel(
                    images = state.images,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Bar Overlays
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 16.dp)
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.content_desc_back)
                    )
                }

                IconButton(
                    onClick = { onIntent(ProductDetailUiIntent.OnToggleFavorite) },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(end = 16.dp, top = 16.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(
                            id = if (state.isFavorite) R.string.content_desc_wishlist_remove else R.string.content_desc_wishlist_add
                        ),
                        tint = if (state.isFavorite) Color.Red else Color.Black
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = state.vendor.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.price,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                StarRatingRow(
                    rating = state.rating,
                    reviewsCount = state.reviewsCount
                )
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.product_detail_select_size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                SizeSelector(
                    sizes = state.availableSizes,
                    selectedSize = state.selectedSize,
                    onSizeSelected = { onIntent(ProductDetailUiIntent.SelectSize(it)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.product_detail_description),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Note: body_html often contains raw HTML tags. 
                // For a proper implementation, this should be parsed by a HTML renderer like HtmlText.
                // Displaying as plain text for now, assuming basic formatting.
                Text(
                    text = state.descriptionHtml,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ProductDetailBottomBar(
    onAddToCartClick: () -> Unit
) {
    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Button(
            onClick = onAddToCartClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1A1B), // Midnight Slate
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(id = R.string.product_detail_add_to_cart),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
