package com.example.wearzone.presentation.categories

import kotlinx.collections.immutable.ImmutableList

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState

    data class Success(
        val categories: ImmutableList<CategoryUiModel>,
        val searchQuery: String = "",
        val cartItemCount: Int = 0,
        ) : CategoriesUiState

    data class Empty(val searchQuery: String = "") : CategoriesUiState

    data class Error(
        val resId: Int,
        val args: String? = null
    ) : CategoriesUiState
}

data class CategoryUiModel(
    val id: Long,
    val name: String,
    val imageUrl: String,
)