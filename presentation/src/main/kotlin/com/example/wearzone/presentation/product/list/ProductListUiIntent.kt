package com.example.wearzone.presentation.product.list

sealed interface ProductListUiIntent {

    data class LoadProducts(
        val collectionId: Long?
    ) : ProductListUiIntent

    data class ProductClicked(
        val productId: Long
    ) : ProductListUiIntent

    data object Retry : ProductListUiIntent
}