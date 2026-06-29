package com.example.wearzone.presentation.home

import com.example.wearzone.domain.product.model.Brand
import com.example.wearzone.domain.product.model.Category
import com.example.wearzone.domain.product.model.Product
import kotlinx.collections.immutable.ImmutableList

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val userName : String,
        val categories: ImmutableList<Category>,
        val brands: ImmutableList<Brand>,
        val trendingProducts: ImmutableList<Product>,
        val newArrivalProducts: ImmutableList<Product>,
        val heroProduct: Product? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
