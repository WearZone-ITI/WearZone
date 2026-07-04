package com.example.wearzone.domain.account.repository

import com.example.wearzone.domain.account.model.OrderCancelReason
import com.example.wearzone.domain.account.model.OrderDetails
import com.example.wearzone.domain.account.model.OrderHistory

interface IOrderHistoryRepository {
    suspend fun getOrders(customerId: Long): Result<List<OrderHistory>>
    suspend fun getOrderDetails(orderId: Long, currentCustomerId: Long): Result<OrderDetails>
    suspend fun cancelOrder(orderId: Long, reason: OrderCancelReason): Result<OrderDetails>
}
