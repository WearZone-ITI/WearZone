package com.wearzone.presentation.product.detail

import kotlinx.collections.immutable.ImmutableList

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    
    data class Error(val message: String) : ProductDetailUiState
    
    data class Success(
        val id: Long,
        val title: String,
        val vendor: String,
        val price: String,
        val descriptionHtml: String,
        val images: ImmutableList<String>,
        val availableSizes: ImmutableList<String>,
        val selectedSize: String? = null,
        // JETS Lab Requirement 2
        val rating: Double,
        val reviewsCount: Int,
        // JETS Lab Requirement 4
        val isFavorite: Boolean,
    ) : ProductDetailUiState
}
