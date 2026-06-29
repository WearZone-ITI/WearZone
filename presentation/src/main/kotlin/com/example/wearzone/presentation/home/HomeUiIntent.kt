package com.example.wearzone.presentation.home

sealed interface HomeUiIntent {
    data object LoadHomeData : HomeUiIntent
    data class OnProductClicked(val productId: String) : HomeUiIntent
    data class OnCategoryClicked(val categoryId: String) : HomeUiIntent
    data class OnBrandClicked(val brandId: String) : HomeUiIntent
}
