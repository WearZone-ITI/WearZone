package com.example.wearzone.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
            TopBar(
                cartItemCount = uiState.cartItemCount,
                onAddToCartClick = { onNavigateToCart() },
                onNavigateBack = onNavigateBack,
            )
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
    val showInitialContent = state.query.isBlank() && !state.hasSearched

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            SearchField(state = state, onIntent = onIntent)
            Spacer(modifier = Modifier.height(20.dp))

            if (showInitialContent && state.recentSearches.isNotEmpty()) {
                RecentSearches(recentSearches = state.recentSearches, onIntent = onIntent)
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (showInitialContent) {
                SearchQuickOptions(state = state, onIntent = onIntent)
                if (state.products.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.search_suggested_products),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            SearchResults(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.weight(1f),
            )
        }
        if (state.isFilterSheetVisible) {
            SearchFilterSheet(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun SearchQuickOptions(
    state: SearchUiState,
    onIntent: (SearchUiIntent) -> Unit,
) {
    val popularCategories = state.categories.take(MAX_INITIAL_FILTER_OPTIONS)
    val popularBrands = state.brands.take(MAX_INITIAL_FILTER_OPTIONS)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (popularCategories.isNotEmpty()) {
            QuickOptionRow(
                title = stringResource(id = R.string.search_popular_categories),
                options = popularCategories,
                onSelected = { categoryTitle ->
                    onIntent(SearchUiIntent.OnCategorySelected(categoryTitle))
                    onIntent(SearchUiIntent.OnApplyFilters)
                },
            )
        }
        if (popularBrands.isNotEmpty()) {
            QuickOptionRow(
                title = stringResource(id = R.string.search_popular_brands),
                options = popularBrands,
                onSelected = { brandTitle ->
                    onIntent(SearchUiIntent.OnBrandSelected(brandTitle))
                    onIntent(SearchUiIntent.OnApplyFilters)
                },
            )
        }
    }
}

@Composable
private fun QuickOptionRow(
    title: String,
    options: List<SearchFilterOptionUiModel>,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(options, key = { it.id }) { option ->
                AssistChip(
                    onClick = { onSelected(option.title) },
                    label = {
                        Text(
                            text = option.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

private const val MAX_INITIAL_FILTER_OPTIONS = 8
