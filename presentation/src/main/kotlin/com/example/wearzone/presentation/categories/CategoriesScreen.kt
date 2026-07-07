package com.example.wearzone.presentation.categories

import com.example.wearzone.presentation.categories.components.CategoryCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.wearzone.presentation.common.PremiumEmptyState
import com.example.wearzone.presentation.common.ProductGridSkeleton
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.common.TopBar
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    navigateToCart : ()-> Unit,
    onNavigateToProductList: (Long, String) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeColors = AppTheme.colors

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when(effect){
                 is CategoriesUiEffect.NavigateToCart ->navigateToCart()
                is CategoriesUiEffect.NavigateToProductList -> {
                    onNavigateToProductList(
                        effect.categoryId,
                        effect.categoryName
                    )
                }

                else -> {}
            }
        }
    }

    Scaffold(
        topBar = { TopBar(cartItemCount = if(uiState is CategoriesUiState.Success) (uiState as CategoriesUiState.Success).cartItemCount else 0,
            onAddToCartClick = { viewModel.handleIntent(CategoriesUiIntent.OnNavigateToCartClick) })
         },
        containerColor = themeColors.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val currentQuery = when (val state = uiState) {
                is CategoriesUiState.Success -> state.searchQuery
                is CategoriesUiState.Empty -> state.searchQuery
                else -> ""
            }

            OutlinedTextField(
                value = currentQuery,
                onValueChange = { newQuery ->
                    viewModel.handleIntent(CategoriesUiIntent.OnSearchQueryChanged(newQuery))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = themeColors.textPrimary),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.categories_search_content_description),
                        tint = themeColors.textSecondary
                    )
                },
                trailingIcon = {
                    if (currentQuery.isNotBlank()) {
                        IconButton(onClick = {
                            viewModel.handleIntent(CategoriesUiIntent.OnSearchQueryChanged(""))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.categories_clear_search_content_description),
                                tint = themeColors.textSecondary
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.categories_search_placeholder),
                        color = themeColors.textSecondary
                    )
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.textPrimary,
                    unfocusedBorderColor = themeColors.divider,
                    focusedContainerColor = themeColors.surface,
                    unfocusedContainerColor = themeColors.surface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is CategoriesUiState.Loading -> {
                    ProductGridSkeleton(modifier = Modifier.fillMaxSize())
                }

                is CategoriesUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                    ) {
                        itemsIndexed(
                            items = state.categories,
                            span = { index, _ ->
                                GridItemSpan(if (index == 0 && currentQuery.isBlank()) 2 else 1)
                            }
                        ) { index, category ->
                            val isFullWidth = (index == 0 && currentQuery.isBlank())

                            CategoryCard(
                                category = category,
                                isFullWidth = isFullWidth,
                                onCategoryClick = {
                                    viewModel.handleIntent(
                                        CategoriesUiIntent.OnCategoryClicked(
                                            categoryId = category.id,
                                            categoryName = category.name
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                is CategoriesUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                           text = if (state.args != null) {
                                stringResource(id = state.resId, state.args)
                            } else {
                                stringResource(id = state.resId)
                            },
                            color = themeColors.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { viewModel.handleIntent(CategoriesUiIntent.OnRetry) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.selected,
                                contentColor = themeColors.onAccent
                            )
                        ) {
                            Text(text = stringResource(id = R.string.categories_retry_button))
                        }
                    }
                }

                is CategoriesUiState.Empty -> {
                    PremiumEmptyState(
                        lottieResId = com.example.presentation.R.raw.no_search_found,
                        title = if (state.searchQuery.isNotBlank()) {
                            stringResource(id = R.string.categories_no_results_found, state.searchQuery)
                        } else {
                            stringResource(id = R.string.categories_no_data_available)
                        },
                        description = "",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}