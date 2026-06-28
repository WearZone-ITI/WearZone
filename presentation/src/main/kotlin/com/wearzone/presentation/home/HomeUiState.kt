package com.wearzone.presentation.home

import com.wearzone.domain.product.model.Brand
import com.wearzone.domain.product.model.Category
import com.wearzone.domain.product.model.Product
import kotlinx.collections.immutable.ImmutableList

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val categories: ImmutableList<Category>,
        val brands: ImmutableList<Brand>,
        val trendingProducts: ImmutableList<Product>,
        val newArrivalProducts: ImmutableList<Product>,
        val heroProduct: Product? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
