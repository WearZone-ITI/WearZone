package com.example.wearzone.domain.account.usecase

import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository

class GetOrderDetailsUseCase(
    private val repository: IOrderHistoryRepository,
) {
    suspend operator fun invoke(orderId: Long, currentCustomerId: Long): Result<OrderDetails> =
        repository.getOrderDetails(orderId, currentCustomerId)
}

