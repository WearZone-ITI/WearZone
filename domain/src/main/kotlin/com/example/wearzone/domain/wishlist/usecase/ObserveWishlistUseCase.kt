package com.example.wearzone.domain.wishlist.usecase

import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.repository.IWishlistRepository
import kotlinx.coroutines.flow.Flow

class ObserveWishlistUseCase(
    private val wishlistRepository: IWishlistRepository
) {
    operator fun invoke(userId: String): Flow<List<WishlistItem>> {
        return wishlistRepository.getWishlistFlow(userId)
    }
}
