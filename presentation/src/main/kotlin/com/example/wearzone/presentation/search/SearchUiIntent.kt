package com.example.wearzone.presentation.search

sealed interface SearchUiIntent {
    data class OnQueryChanged(val query: String) : SearchUiIntent
    data object OnSearchSubmitted : SearchUiIntent
    data class OnRecentSearchClicked(val query: String) : SearchUiIntent
    data object OnClearRecentSearches : SearchUiIntent
    data object OnFilterClicked : SearchUiIntent
    data object OnDismissFilters : SearchUiIntent
    data class OnBrandSelected(val brandTitle: String?) : SearchUiIntent
    data class OnCategorySelected(val categoryTitle: String?) : SearchUiIntent
    data class OnMinPriceChanged(val minPrice: String) : SearchUiIntent
    data class OnMaxPriceChanged(val maxPrice: String) : SearchUiIntent
    data object OnApplyFilters : SearchUiIntent
    data object OnResetFilters : SearchUiIntent
    data object OnRetry : SearchUiIntent
    data class OnProductClicked(val productId: String) : SearchUiIntent
}
