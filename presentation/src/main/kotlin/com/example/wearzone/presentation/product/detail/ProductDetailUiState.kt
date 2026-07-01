package com.example.wearzone.presentation.product.detail

import kotlinx.collections.immutable.ImmutableList

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    
    data class Error(@androidx.annotation.StringRes val messageRes: Int) : ProductDetailUiState
    
    data class Success(
        val id: Long,
        val title: String,
        val vendor: String,
        val price: String,
        val descriptionHtml: String,
        val images: ImmutableList<String>,
        val availableSizes: ImmutableList<String>,
        val selectedSize: String? = null,
        val rating: Double,
        val reviewsCount: Int,
        val isFavorite: Boolean,
        val showRemoveDialog: Boolean = false
    ) : ProductDetailUiState
}
