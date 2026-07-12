package com.example.wearzone.presentation.home

import com.example.wearzone.domain.common.Category
import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val userName : String,
        val categories: ImmutableList<Category>,
        val brands: ImmutableList<Brand>,
        val trendingProducts: ImmutableList<Product>,
        val newArrivalProducts: ImmutableList<Product>,
        val heroProduct: Product? = null,
        val cartItemCount: Int = 0,
        val productToRemove: Product? = null,
        val promoAds: ImmutableList<Product> = persistentListOf()
    ) : HomeUiState
    data class Error(
        val message: String,
        val offlineFavorites: ImmutableList<Product> = persistentListOf()
    ) : HomeUiState
}
