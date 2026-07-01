package com.example.wearzone.domain.wishlist.usecase

import com.example.wearzone.domain.wishlist.repository.IWishlistRepository

class SyncWishlistUseCase(
    private val wishlistRepository: IWishlistRepository
) {
    suspend operator fun invoke(userId: String) {
        wishlistRepository.syncWithRemote(userId)
    }
}
