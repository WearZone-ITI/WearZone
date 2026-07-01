package com.example.wearzone.domain.wishlist.usecase

import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.repository.IWishlistRepository

class ToggleFavoriteUseCase(
    private val wishlistRepository: IWishlistRepository
) {
    suspend operator fun invoke(item: WishlistItem, userId: String) {
        wishlistRepository.toggleFavorite(item, userId)
    }
}
