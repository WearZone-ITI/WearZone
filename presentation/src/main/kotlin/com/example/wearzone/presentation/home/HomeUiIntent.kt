package com.example.wearzone.presentation.home
import com.example.wearzone.domain.product.model.Product

sealed interface HomeUiIntent {
    data object LoadHomeData : HomeUiIntent
    data class OnProductClicked(val productId: String) : HomeUiIntent
    data class OnCategoryClicked(val categoryId: String) : HomeUiIntent
    data class OnBrandClicked(val brandId: String) : HomeUiIntent
    data object OnCartClicked : HomeUiIntent
    data class OnAddToCartClicked(val product: Product) : HomeUiIntent
    data class OnFavoriteClicked(val product: Product) : HomeUiIntent
    data object OnConfirmRemove : HomeUiIntent
    data object OnCancelRemove : HomeUiIntent
}
