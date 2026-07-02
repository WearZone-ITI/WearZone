package com.example.wearzone.domain.account.repository

import com.example.wearzone.domain.account.model.OrderHistory

interface IOrderHistoryRepository {
    suspend fun getOrders(customerId: Long): Result<List<OrderHistory>>
}
