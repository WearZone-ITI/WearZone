package com.example.wearzone.presentation.product.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.wearzone.domain.product.model.Product
import com.example.wearzone.presentation.categories.CategoriesUiIntent
import com.example.wearzone.presentation.common.ProductTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    collectionId: Long,
    categoryName: String,
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCart : ()-> Unit,
    viewModel: ProductListViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(collectionId) {
        viewModel.onIntent(
            ProductListUiIntent.LoadProducts(collectionId)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect {
            when(it){
                is ProductListUiEffect.NavigateToCart -> onNavigateToCart()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            ProductTopBar(
                title = categoryName,
                cartItemCount = uiState.cartItemCount,
                onBackClick = onNavigateBack,
                onCartClick = { viewModel.onIntent(ProductListUiIntent.OnCartClicked)}
            )
        }
    ) { padding ->

        when {

            uiState.isLoading -> {

                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                uiState.error?.let { errorMessage ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = errorMessage)
                    }
                }
            }

            else -> {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(uiState.products) { product ->

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

    }

}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Crop
        )

        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2
            )

            Text(
                text = "$${product.price}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

        }

    }

}