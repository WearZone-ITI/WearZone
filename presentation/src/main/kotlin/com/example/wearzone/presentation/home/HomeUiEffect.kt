package com.example.wearzone.presentation.home

sealed interface HomeUiEffect {
    data class NavigateToProductDetail(val productId: String) : HomeUiEffect
    data class NavigateToCategory(val categoryId: String) : HomeUiEffect
    data class NavigateToBrand(val brandId: String) : HomeUiEffect
    data class ShowSnackbar(val message: String) : HomeUiEffect
}
