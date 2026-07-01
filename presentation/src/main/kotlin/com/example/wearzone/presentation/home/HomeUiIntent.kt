package com.example.wearzone.presentation.home

sealed interface HomeUiIntent {
    data object LoadHomeData : HomeUiIntent
    data class OnProductClicked(val productId: String) : HomeUiIntent
    data class OnCategoryClicked(val categoryId: String) : HomeUiIntent
    data class OnBrandClicked(val brandId: String) : HomeUiIntent
    data object OnCartClicked : HomeUiIntent
    data class OnAddToCartClicked(val product: com.example.wearzone.domain.product.model.Product) : HomeUiIntent
}
