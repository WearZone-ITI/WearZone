package com.example.wearzone.presentation.brands

sealed interface BrandsUiIntent {
    data object LoadBrands : BrandsUiIntent
    data class OnBrandClicked(val brandName: String) : BrandsUiIntent
}
