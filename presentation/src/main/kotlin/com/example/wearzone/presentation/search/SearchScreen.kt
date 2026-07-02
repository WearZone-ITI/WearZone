package com.example.wearzone.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.common.TopBar
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.search.components.RecentSearches
import com.example.wearzone.presentation.search.components.SearchField
import com.example.wearzone.presentation.search.components.SearchFilterSheet
import com.example.wearzone.presentation.search.components.SearchResults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCart: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SearchUiEffect.NavigateToProductDetail -> onNavigateToProductDetail(effect.productId)
            }
        }
    }

    Scaffold(
        topBar = { 
            TopBar(cartItemCount = uiState.cartItemCount, onAddToCartClick = { onNavigateToCart() })
        },
        containerColor = AppTheme.colors.background,
    ) { paddingValues ->
        SearchContent(
            state = uiState,
            modifier = Modifier.padding(paddingValues),
            onIntent = viewModel::handleIntent,
        )
    }
}

@Composable
private fun SearchContent(
    state: SearchUiState,
    modifier: Modifier = Modifier,
    onIntent: (SearchUiIntent) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            SearchField(state = state, onIntent = onIntent)
            Spacer(modifier = Modifier.height(20.dp))
            if (state.query.isBlank() && state.recentSearches.isNotEmpty()) {
                RecentSearches(recentSearches = state.recentSearches, onIntent = onIntent)
                Spacer(modifier = Modifier.height(20.dp))
            }
            SearchResults(state = state, onIntent = onIntent)
        }
        if (state.isFilterSheetVisible) {
            SearchFilterSheet(state = state, onIntent = onIntent)
        }
    }
}