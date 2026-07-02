package com.example.wearzone.domain.cart.usecase

import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.common.DataResult

class AddToCartUseCase(
    private val repository: ICartRepository,
) {
    suspend operator fun invoke(item: CartItem): DataResult<Unit> = repository.addToCart(item)
}
