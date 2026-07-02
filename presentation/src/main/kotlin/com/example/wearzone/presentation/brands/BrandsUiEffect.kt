package com.example.wearzone.presentation.brands

sealed interface BrandsUiEffect {
    data class NavigateToVendorProducts(val vendorName: String) : BrandsUiEffect
}
