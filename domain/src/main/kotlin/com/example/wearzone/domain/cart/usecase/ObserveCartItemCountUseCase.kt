package com.example.wearzone.domain.cart.usecase

import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.cart.repository.ICartRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest

class ObserveCartItemCountUseCase(
    private val repository: ICartRepository,
    private val getAuthAccessStateUseCase: GetAuthAccessStateUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Int> = repository.observeCart()
        .mapLatest { items ->
            if (getAuthAccessStateUseCase() is AuthAccessState.AuthenticatedCustomer) {
                items.sumOf { it.quantity }
            } else {
                0
            }
        }
        .distinctUntilChanged()
}
