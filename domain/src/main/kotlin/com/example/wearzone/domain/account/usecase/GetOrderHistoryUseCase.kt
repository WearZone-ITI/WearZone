package com.example.wearzone.domain.account.usecase

import com.example.wearzone.domain.account.model.OrderHistory
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository

class GetOrderHistoryUseCase(
    private val repository: IOrderHistoryRepository,
) {
    suspend operator fun invoke(customerId: Long): Result<List<OrderHistory>> =
        repository.getOrders(customerId)
}
