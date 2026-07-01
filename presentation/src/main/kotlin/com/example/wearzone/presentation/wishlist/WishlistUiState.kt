package com.example.wearzone.presentation.wishlist

import com.example.wearzone.domain.wishlist.model.WishlistItem
import kotlinx.collections.immutable.ImmutableList

sealed interface WishlistUiState {
    data object Loading : WishlistUiState
    data object GuestState : WishlistUiState
    data class Success(
        val items: ImmutableList<WishlistItem>,
        val itemToRemove: WishlistItem? = null
    ) : WishlistUiState
    data class Error(val message: String) : WishlistUiState
}
