package com.example.wearzone.presentation.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.example.wearzone.presentation.common.ProductGridSkeleton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.common.NetworkErrorState
import com.example.wearzone.presentation.common.PremiumEmptyState
import com.example.wearzone.presentation.common.theme.AppColors
import com.example.wearzone.presentation.search.SearchUiIntent
import com.example.wearzone.presentation.search.SearchUiState

@Composable
fun SearchResults(
    state: SearchUiState,
    onIntent: (SearchUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> ProductGridSkeleton(modifier = modifier)
        state.hasError -> NetworkErrorState(
            modifier = modifier.fillMaxSize(),
            onRetry = { onIntent(SearchUiIntent.OnRetry) },
        )
        state.products.isEmpty() && state.hasSearched -> PremiumEmptyState(
            modifier = modifier,
            lottieResId = R.raw.no_search_found,
            title = stringResource(id = R.string.search_empty_title),
            description = stringResource(id = R.string.search_empty_subtitle),
        )
        state.products.isEmpty() && state.hasUsefulInitialContent() -> Box(modifier = modifier.fillMaxSize())
        state.products.isEmpty() -> SearchMessage(
            title = stringResource(id = R.string.search_start_title),
            subtitle = stringResource(id = R.string.search_start_subtitle),
            modifier = modifier,
        )
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(state.products, key = { it.id }) { product ->
                SearchProductCard(product = product, onClick = {
                    onIntent(SearchUiIntent.OnProductClicked(product.id))
                })
            }
        }
    }
}

private fun SearchUiState.hasUsefulInitialContent(): Boolean =
    !hasSearched && (recentSearches.isNotEmpty() || categories.isNotEmpty() || brands.isNotEmpty())
