package com.example.wearzone.domain.cart.usecase

import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.common.DataResult

class ClearCartUseCase(
    private val repository: ICartRepository,
) {
    suspend operator fun invoke(): DataResult<Unit> = repository.clearCart()
}
