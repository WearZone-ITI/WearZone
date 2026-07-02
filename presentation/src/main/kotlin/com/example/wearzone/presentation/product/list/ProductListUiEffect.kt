package com.example.wearzone.presentation.product.list

sealed interface ProductListUiEffect {

    data class NavigateToProductDetails(
        val productId: Long
    ) : ProductListUiEffect

    data class ShowError(
        val message: String
    ) : ProductListUiEffect
}