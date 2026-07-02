package com.example.wearzone.presentation.vendor_products

sealed interface VendorProductsUiEffect {
    data class NavigateToProductDetail(val productId: String) : VendorProductsUiEffect
    data class ShowSnackbar(val messageResId: Int) : VendorProductsUiEffect
}
