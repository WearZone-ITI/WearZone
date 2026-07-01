package com.example.wearzone.domain.wishlist.usecase

import com.example.wearzone.domain.wishlist.model.WishlistItem
import com.example.wearzone.domain.wishlist.repository.IWishlistRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private val repository = mockk<IWishlistRepository>(relaxed = true)
    private val useCase = ToggleFavoriteUseCase(repository)

    @Test
    fun `invoke calls toggleFavorite on repository`() = runTest {
        val item = WishlistItem(
            id = "1",
            title = "Title",
            vendor = "Vendor",
            price = "10.0",
            currencyCode = "USD",
            imageUrl = "url",
            isOutOfStock = false
        )
        val userId = "user123"

        useCase.invoke(item, userId)

        coVerify { repository.toggleFavorite(item, userId) }
    }
}
