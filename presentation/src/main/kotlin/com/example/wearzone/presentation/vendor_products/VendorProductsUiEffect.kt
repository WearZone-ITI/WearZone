package com.example.wearzone.presentation.vendor_products

import androidx.annotation.StringRes

sealed interface VendorProductsUiEffect {
    data class NavigateToProductDetail(val productId: String) : VendorProductsUiEffect
    data class ShowSignInRequired(@StringRes val messageResId: Int) : VendorProductsUiEffect
    data class ShowSnackbar(@StringRes val messageResId: Int) : VendorProductsUiEffect
}
