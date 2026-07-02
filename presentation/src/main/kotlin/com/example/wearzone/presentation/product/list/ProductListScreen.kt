package com.example.wearzone.presentation.product.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.wearzone.presentation.common.ProductTopBar

data class ProductUi(
    val id: String,
    val name: String,
    val price: String,
    val image: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    brandId: String?,
    categoryName: String?,
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit
) {

    val products = listOf(
        ProductUi(
            "1",
            "Oversized T-Shirt",
            "$35",
            "https://picsum.photos/300/400?1"
        ),
        ProductUi(
            "2",
            "Slim Fit Jeans",
            "$60",
            "https://picsum.photos/300/400?2"
        ),
        ProductUi(
            "3",
            "Sneakers",
            "$120",
            "https://picsum.photos/300/400?3"
        ),
        ProductUi(
            "4",
            "Hoodie",
            "$75",
            "https://picsum.photos/300/400?4"
        ),
        ProductUi(
            "5",
            "Cargo Pants",
            "$50",
            "https://picsum.photos/300/400?5"
        ),
        ProductUi(
            "6",
            "Jacket",
            "$95",
            "https://picsum.photos/300/400?6"
        )
    )

    Scaffold(
        topBar = {
            ProductTopBar(
                title = categoryName ?: "Products",
                cartItemCount = 2,
                onBackClick = onNavigateBack,
                onCartClick = {
                    // Navigate to Cart
                }
            )
        }
    ){ padding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(products) { product ->

                ProductCard(
                    product = product,
                    onClick = {
                        onNavigateToProductDetail(product.id)
                    }
                )
            }

        }

    }

}

@Composable
fun ProductCard(
    product: ProductUi,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column {

            AsyncImage(
                model = product.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {

                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = product.price,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )

            }

        }

    }

}