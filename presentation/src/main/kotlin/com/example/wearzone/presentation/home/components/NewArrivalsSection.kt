package com.example.wearzone.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun NewArrivalsSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onAddToCartClick: (Product) -> Unit = {},
    onFavoriteClick: (Product) -> Unit = {}
) {
    Column {
        Text(
            text = stringResource(id = R.string.home_new_arrivals),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(products) { product ->
                ProductCard(product, onProductClick, onAddToCartClick,onFavoriteClick)
            }
        }
    }
}