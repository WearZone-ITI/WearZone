package com.example.wearzone.presentation.vendor_products

import com.example.wearzone.domain.product.model.Product
import kotlinx.collections.immutable.ImmutableList

sealed interface VendorProductsUiState {
    data object Loading : VendorProductsUiState
    data class Success(
        val products: ImmutableList<Product>,
        val vendorName: String
    ) : VendorProductsUiState
    data class Error(val message: String) : VendorProductsUiState
}
