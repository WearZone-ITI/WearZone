package com.example.wearzone.domain.cart.usecase

import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.common.DataResult

class UpdateCartQuantityUseCase(
    private val repository: ICartRepository,
) {
    suspend operator fun invoke(variantId: String, quantity: Int): DataResult<Unit> =
        repository.updateQuantity(variantId, quantity)
}
