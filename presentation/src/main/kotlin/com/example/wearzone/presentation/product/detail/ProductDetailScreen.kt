package com.example.wearzone.presentation.product.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.common.NetworkErrorState
import com.example.wearzone.presentation.common.SignInRequiredDialog
import com.example.wearzone.presentation.common.formatPrice
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.product.detail.components.ImageCarousel
import com.example.wearzone.presentation.product.detail.components.ProductDetailShimmer
import com.example.wearzone.presentation.product.detail.components.ReviewListCard
import com.example.wearzone.presentation.product.detail.components.SizeSelector
import com.example.wearzone.presentation.product.detail.components.StarRatingRow
import com.example.wearzone.presentation.product.detail.components.WriteReviewBottomSheet
import com.example.wearzone.presentation.wishlist.components.RemoveFavoriteDialog
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignInRequiredDialog by remember { mutableStateOf(false) }
    var showWriteReviewBottomSheet by remember { mutableStateOf(false) }
    var signInRequiredMessageRes by remember { mutableStateOf(R.string.sign_in_required_message) }

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ProductDetailUiEffect.ShowToast -> snackbarHostState.showSnackbar(
                    context.resources.getString(
                        effect.messageRes
                    )
                )

                is ProductDetailUiEffect.ShowSignInRequired -> {
                    signInRequiredMessageRes = effect.messageRes
                    showSignInRequiredDialog = true
                }

                is ProductDetailUiEffect.OpenWriteReviewSheet -> {
                    showWriteReviewBottomSheet = true
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (uiState is ProductDetailUiState.Success) {
                val successState = uiState as ProductDetailUiState.Success
                ProductDetailBottomBar(
                    quantityInCart = successState.quantityInCart,
                    addToCartEnabled = !successState.isOutOfStock,
                    onAddToCartClick = { viewModel.handleIntent(ProductDetailUiIntent.OnAddToCartClick) },
                    onWriteReviewClick = { viewModel.handleIntent(ProductDetailUiIntent.OnWriteReviewClick) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when (val state = uiState) {
                is ProductDetailUiState.Loading -> {
                    ProductDetailShimmer(onNavigateBack = onNavigateBack)
                }

                is ProductDetailUiState.Error -> {
                    NetworkErrorState(
                        modifier = Modifier.fillMaxSize(),
                        onRetry = { viewModel.handleIntent(ProductDetailUiIntent.Retry) }
                    )
                }

                is ProductDetailUiState.Success -> {
                    if (state.showRemoveDialog) {
                        RemoveFavoriteDialog(
                            onConfirm = { viewModel.handleIntent(ProductDetailUiIntent.OnConfirmRemove) },
                            onDismiss = { viewModel.handleIntent(ProductDetailUiIntent.OnCancelRemove) })
                    }
                    ProductDetailContent(
                        state = state,
                        onNavigateBack = onNavigateBack,
                        onIntent = { viewModel.handleIntent(it) })
                }
            }
        }
    }

    if (showSignInRequiredDialog) {
        SignInRequiredDialog(
            messageRes = signInRequiredMessageRes,
            onSignInRegister = {
                showSignInRequiredDialog = false
                onNavigateToLogin()
            },
            onContinueBrowsing = { showSignInRequiredDialog = false },
        )
    }

    if (showWriteReviewBottomSheet) {
        WriteReviewBottomSheet(
            onDismissRequest = { showWriteReviewBottomSheet = false },
            onSubmit = { rating, comment ->
                showWriteReviewBottomSheet = false
                viewModel.handleIntent(ProductDetailUiIntent.SubmitReview(rating, comment))
            }
        )
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
                    images = state.images, modifier = Modifier.fillMaxSize()
                )

                // Top Bar Overlays
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.content_desc_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { onIntent(ProductDetailUiIntent.OnToggleFavorite) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(
                                id = if (state.isFavorite) R.string.content_desc_wishlist_remove else R.string.content_desc_wishlist_add
                            ),
                            tint = if (state.isFavorite) AppTheme.colors.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-32).dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = state.vendor.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatPrice(state.basePriceEgp),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRatingRow(
                        rating = state.rating, reviewsCount = state.reviewsCount
                    )

                    if (state.isOutOfStock) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.cart_out_of_stock),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (state.selectedVariantQuantity in 1..5) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.cart_low_stock_format, state.selectedVariantQuantity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (state.availableSizes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.product_detail_select_size),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SizeSelector(
                            sizes = state.availableSizes,
                            selectedSize = state.selectedSize,
                            onSizeSelected = { onIntent(ProductDetailUiIntent.SelectSize(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (state.availableColors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.product_detail_select_color),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SizeSelector(
                            sizes = state.availableColors,
                            selectedSize = state.selectedColor,
                            onSizeSelected = { onIntent(ProductDetailUiIntent.SelectColor(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(id = R.string.product_detail_description),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.descriptionHtml,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(id = R.string.product_detail_reviews_header),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (state.reviewsCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.product_detail_avg_rating, state.rating),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.reviews.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.product_detail_no_reviews),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        )
                    } else {
                        state.reviews.forEach { review ->
                            ReviewListCard(
                                review = review,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductDetailBottomBar(
    quantityInCart: Int,
    addToCartEnabled: Boolean,
    onAddToCartClick: () -> Unit,
    onWriteReviewClick: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onWriteReviewClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = CircleShape,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.product_detail_write_review_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onAddToCartClick,
                enabled = addToCartEnabled,
                modifier = Modifier.height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BadgedBox(
                        badge = {
                            if (quantityInCart > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(
                                        text = quantityInCart.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (quantityInCart > 0) {
                            stringResource(id = R.string.product_detail_add_to_cart_with_qty, quantityInCart)
                        } else {
                            stringResource(id = R.string.product_detail_add_to_cart)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
