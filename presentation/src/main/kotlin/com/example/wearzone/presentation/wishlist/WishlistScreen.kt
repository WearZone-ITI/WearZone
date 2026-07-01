package com.example.wearzone.presentation.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.TopBar
import com.example.wearzone.presentation.wishlist.components.RemoveFavoriteDialog
import com.example.wearzone.presentation.wishlist.components.WishlistEmptyState
import com.example.wearzone.presentation.wishlist.components.WishlistProductCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WishlistScreen(
    onNavigateToProductDetail: (String) -> Unit,
    onShowSnackbar: suspend (String) -> Unit,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is WishlistUiEffect.NavigateToProductDetail -> onNavigateToProductDetail(effect.productId)
                is WishlistUiEffect.ShowSnackbar -> onShowSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = { TopBar() },
        containerColor = AppTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 24.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.wishlist_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppTheme.colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val subtitle = when (uiState) {
                    is WishlistUiState.Success -> stringResource(id = R.string.wishlist_items_saved, (uiState as WishlistUiState.Success).items.size)
                    is WishlistUiState.GuestState -> stringResource(id = R.string.wishlist_sign_in_required)
                    else -> ""
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Content
            when (val state = uiState) {
                is WishlistUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppTheme.colors.selected)
                    }
                }
                is WishlistUiState.GuestState -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.wishlist_sign_in_required),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.colors.textSecondary
                        )
                        // TODO: Implement the Guest Modal matching VogueVibe HTML design here later.
                    }
                }
                is WishlistUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = AppTheme.colors.error
                        )
                    }
                }
                is WishlistUiState.Success -> {
                    if (state.itemToRemove != null) {
                        RemoveFavoriteDialog(
                            onConfirm = { viewModel.handleIntent(WishlistUiIntent.OnConfirmRemove) },
                            onDismiss = { viewModel.handleIntent(WishlistUiIntent.OnCancelRemove) }
                        )
                    }
                    
                    if (state.items.isEmpty()) {
                        WishlistEmptyState()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                WishlistProductCard(
                                    item = item,
                                    onProductClick = { viewModel.handleIntent(WishlistUiIntent.OnProductClicked(it)) },
                                    onRemoveClick = { viewModel.handleIntent(WishlistUiIntent.OnRemoveClicked(it)) },
                                    onAddToCartClick = {
                                        // Normally handled by a Cart UseCase, for now just show a snackbar
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
