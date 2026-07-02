package com.example.wearzone.presentation.vendor_products

import com.example.wearzone.domain.product.model.Product

sealed interface VendorProductsUiIntent {
    data object LoadProducts : VendorProductsUiIntent
    data class OnProductClicked(val productId: String) : VendorProductsUiIntent
    data class OnAddToCartClicked(val product: Product) : VendorProductsUiIntent
    data class OnFavoriteClicked(val product: Product) : VendorProductsUiIntent
    data object OnRetry : VendorProductsUiIntent
}
