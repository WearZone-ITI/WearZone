package com.example.wearzone.presentation.search.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.wearzone.presentation.common.ProductGridSkeleton

@Composable
fun SearchLoadingComponent(
    modifier: Modifier = Modifier
) {
    ProductGridSkeleton(modifier = modifier)
}