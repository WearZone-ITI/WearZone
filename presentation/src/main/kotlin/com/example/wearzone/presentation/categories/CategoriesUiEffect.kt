package com.example.wearzone.presentation.categories

sealed interface CategoriesUiEffect {
    data class NavigateToProductList(val categoryId: Long, val categoryName: String) : CategoriesUiEffect
    data class ShowError(val message: String) : CategoriesUiEffect
    data object NavigateToCart : CategoriesUiEffect
}
