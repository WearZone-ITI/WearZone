package com.example.wearzone.presentation.wishlist

import androidx.annotation.StringRes
import com.example.wearzone.domain.wishlist.model.WishlistItem
import kotlinx.collections.immutable.ImmutableList

sealed interface WishlistUiState {
    data object Loading : WishlistUiState
    data object GuestState : WishlistUiState
    data class Success(
        val items: ImmutableList<WishlistItem>,
        val itemToRemove: WishlistItem? = null,
        val cartItemCount: Int = 0,
        ) : WishlistUiState
    data class Error(@StringRes val messageRes: Int) : WishlistUiState
}
