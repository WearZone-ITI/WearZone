package com.example.wearzone.presentation.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.common.NetworkErrorState
import com.example.wearzone.presentation.common.PremiumEmptyState
import com.example.wearzone.presentation.common.ProductGridSkeleton
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.search.SearchFilterOptionUiModel
import com.example.wearzone.presentation.search.SearchUiIntent
import com.example.wearzone.presentation.search.SearchUiState

@Composable
fun SearchResults(
    state: SearchUiState,
    onIntent: (SearchUiIntent) -> Unit,
    modifier: Modifier = Modifier,
    onCategorySelected: (SearchFilterOptionUiModel) -> Unit = {},
    onBrandSelected: (SearchFilterOptionUiModel) -> Unit = {},
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
        state.products.isEmpty() && !state.hasUsefulInitialContent() -> SearchMessage(
            title = stringResource(id = R.string.search_start_title),
            subtitle = stringResource(id = R.string.search_start_subtitle),
            modifier = modifier,
        )
        else -> SearchScrollableContent(
            state = state,
            onIntent = onIntent,
            onCategorySelected = onCategorySelected,
            onBrandSelected = onBrandSelected,
            modifier = modifier,
        )
    }
}

@Composable
private fun SearchScrollableContent(
    state: SearchUiState,
    onIntent: (SearchUiIntent) -> Unit,
    onCategorySelected: (SearchFilterOptionUiModel) -> Unit,
    onBrandSelected: (SearchFilterOptionUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showInitialContent = state.query.isBlank() && !state.hasSearched
    val popularCategories = state.categories.take(MAX_INITIAL_FILTER_OPTIONS)
    val popularBrands = state.brands.take(MAX_INITIAL_FILTER_OPTIONS)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (showInitialContent && state.recentSearches.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                RecentSearches(recentSearches = state.recentSearches, onIntent = onIntent)
            }
        }

        if (showInitialContent && popularCategories.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                QuickOptionRow(
                    title = stringResource(id = R.string.search_popular_categories),
                    options = popularCategories,
                    onSelected = onCategorySelected,
                )
            }
        }

        if (showInitialContent && popularBrands.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                QuickOptionRow(
                    title = stringResource(id = R.string.search_popular_brands),
                    options = popularBrands,
                    onSelected = onBrandSelected,
                )
            }
        }

        if (showInitialContent && state.products.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(id = R.string.search_suggested_products),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        items(state.products, key = { it.id }) { product ->
            SearchProductCard(
                product = product,
                onClick = { onIntent(SearchUiIntent.OnProductClicked(product.id)) },
            )
        }
    }
}

@Composable
private fun QuickOptionRow(
    title: String,
    options: List<SearchFilterOptionUiModel>,
    onSelected: (SearchFilterOptionUiModel) -> Unit,
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
                    onClick = { onSelected(option) },
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

private fun SearchUiState.hasUsefulInitialContent(): Boolean =
    !hasSearched && (recentSearches.isNotEmpty() || categories.isNotEmpty() || brands.isNotEmpty())

private const val MAX_INITIAL_FILTER_OPTIONS = 8
