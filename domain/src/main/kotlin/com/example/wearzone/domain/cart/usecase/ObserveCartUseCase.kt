package com.example.wearzone.domain.cart.usecase

import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.repository.ICartRepository
import kotlinx.coroutines.flow.Flow

class ObserveCartUseCase(
    private val repository: ICartRepository,
) {
    operator fun invoke(): Flow<List<CartItem>> = repository.observeCart()
}
