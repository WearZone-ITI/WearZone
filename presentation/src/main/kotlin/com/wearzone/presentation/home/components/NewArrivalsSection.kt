package com.wearzone.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wearzone.domain.product.model.Product

@Composable
fun NewArrivalsSection(products: List<Product>, onProductClick: (String) -> Unit) {
    Column {
        Text(
            text = "New Arrivals",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(products) { product ->
                ProductCard(product, onProductClick)
            }
        }
    }
}