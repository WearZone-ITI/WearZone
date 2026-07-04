package com.example.wearzone.presentation.vendor_products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.common.SignInRequiredDialog
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.home.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProductsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: VendorProductsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignInRequiredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is VendorProductsUiEffect.NavigateToProductDetail -> onNavigateToProductDetail(effect.productId)
                VendorProductsUiEffect.ShowSignInRequired -> showSignInRequiredDialog = true
                is VendorProductsUiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId))
                }
            }
        }
    }

    val title = when (val state = uiState) {
        is VendorProductsUiState.Success -> state.vendorName
        else -> stringResource(R.string.vendor_products_title)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AppTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is VendorProductsUiState.Loading -> {
                    CircularProgressIndicator(color = AppTheme.colors.selected)
                }
                is VendorProductsUiState.Error -> {
                    Text(
                        text = state.message,
                        color = AppTheme.colors.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable { viewModel.handleIntent(VendorProductsUiIntent.OnRetry) }
                    )
                }
                is VendorProductsUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.vendor_products_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.colors.textSecondary
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.products) { product ->
                                ProductCard(
                                    product = product,
                                    onProductClick = { id ->
                                        viewModel.handleIntent(VendorProductsUiIntent.OnProductClicked(id))
                                    },
                                    onAddToCartClick = { prod ->
                                        viewModel.handleIntent(VendorProductsUiIntent.OnAddToCartClicked(prod))
                                    },
                                    onFavoriteClick = { prod ->
                                        viewModel.handleIntent(VendorProductsUiIntent.OnFavoriteClicked(prod))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSignInRequiredDialog) {
        SignInRequiredDialog(
            onSignIn = {
                showSignInRequiredDialog = false
                onNavigateToLogin()
            },
            onCreateAccount = {
                showSignInRequiredDialog = false
                onNavigateToRegister()
            },
            onContinueBrowsing = { showSignInRequiredDialog = false },
        )
    }
}
