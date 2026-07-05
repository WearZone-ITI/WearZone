package com.example.wearzone.presentation.product.detail

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    
    data class Error(@androidx.annotation.StringRes val messageRes: Int) : ProductDetailUiState
    
    data class Success(
        val id: String,
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
        val showRemoveDialog: Boolean = false,
        val reviews: ImmutableList<ClientReviewUiModel> = persistentListOf(),
        val quantityInCart: Int = 0
    ) : ProductDetailUiState
}

data class ClientReviewUiModel(
    val id: String,
    val shopperName: String,
    val rating: Double,
    val comment: String,
    val formattedDate: String
)

