package com.example.wearzone.presentation.wishlist

import com.example.wearzone.domain.wishlist.model.WishlistItem

sealed interface WishlistUiIntent {
    data class OnProductClicked(val productId: String) : WishlistUiIntent
    data class OnRemoveClicked(val item: WishlistItem) : WishlistUiIntent
    data object OnConfirmRemove : WishlistUiIntent
    data object OnCancelRemove : WishlistUiIntent
    data object OnCartClicked : WishlistUiIntent
}
