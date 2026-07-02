package com.example.wearzone.presentation.brands

import com.example.wearzone.domain.product.model.Brand
import kotlinx.collections.immutable.ImmutableList

sealed interface BrandsUiState {
    data object Loading : BrandsUiState
    data class Success(val brands: ImmutableList<Brand>) : BrandsUiState
    data class Error(val message: String) : BrandsUiState
}
