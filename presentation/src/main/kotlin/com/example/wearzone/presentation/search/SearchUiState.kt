package com.example.wearzone.presentation.search

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SearchUiState(
    val query: String = "",
    val products: ImmutableList<ProductSearchUiModel> = persistentListOf(),
    val recentSearches: ImmutableList<String> = persistentListOf(),
    val brands: ImmutableList<SearchFilterOptionUiModel> = persistentListOf(),
    val categories: ImmutableList<SearchFilterOptionUiModel> = persistentListOf(),
    val selectedBrandTitle: String? = null,
    val selectedCategoryTitle: String? = null,
    val minPrice: String = "",
    val maxPrice: String = "",
    val isLoading: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val hasSearched: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
)

data class ProductSearchUiModel(
    val id: String,
    val title: String,
    val vendor: String,
    val formattedPrice: String,
    val imageUrl: String?,
)

data class SearchFilterOptionUiModel(
    val id: String,
    val title: String,
)