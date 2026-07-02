package com.example.wearzone.domain.cart.usecase

import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.common.DataResult

class RemoveFromCartUseCase(
    private val repository: ICartRepository,
) {
    suspend operator fun invoke(variantId: String): DataResult<Unit> = repository.removeFromCart(variantId)
}
