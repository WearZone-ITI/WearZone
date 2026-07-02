package com.example.wearzone.presentation.product.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProductListScreen(
    brandId: String?,
    categoryName: String?,
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Product List Screen: $categoryName")
    }
}