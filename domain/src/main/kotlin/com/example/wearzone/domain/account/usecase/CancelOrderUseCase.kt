package com.example.wearzone.domain.account.usecase

import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository

class CancelOrderUseCase(
    private val repository: IOrderHistoryRepository,
) {
    suspend operator fun invoke(
        orderId: Long,
        reason: OrderCancelReason = OrderCancelReason.Customer,
    ): Result<OrderDetails> = repository.cancelOrder(orderId, reason)
}

