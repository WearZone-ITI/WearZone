package com.example.wearzone.presentation.categories

sealed interface CategoriesUiIntent {
    data class OnSearchQueryChanged(val query: String) : CategoriesUiIntent
    data class OnCategoryClicked(val categoryId: String, val categoryName: String) : CategoriesUiIntent
    data object OnRetry : CategoriesUiIntent
    data object OnNavigateToCartClick : CategoriesUiIntent
}
