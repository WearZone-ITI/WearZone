package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.dto.OrderDto
import com.example.wearzone.data.remote.dto.ShopifyOrderRequestDto

interface IOrderRemoteDataSource {
    suspend fun getOrders(customerId: Long): List<OrderDto>
    suspend fun createOrder(request: ShopifyOrderRequestDto): OrderDto
}
