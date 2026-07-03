package com.example.wearzone.data.remote.datasource

import com.example.wearzone.data.remote.api.OrderApiService
import com.example.wearzone.data.remote.dto.OrderDto
import com.example.wearzone.data.remote.dto.ShopifyCancelOrderRequestDto
import com.example.wearzone.data.remote.dto.ShopifyOrderRequestDto
import javax.inject.Inject

class OrderRemoteDataSourceImpl @Inject constructor(
    private val apiService: OrderApiService,
) : IOrderRemoteDataSource {

    override suspend fun getOrders(customerId: Long): List<OrderDto> =
        apiService.getOrders(
            version = SHOPIFY_API_VERSION,
            customerId = customerId,
        ).orders

    override suspend fun getOrder(orderId: Long): OrderDto =
        apiService.getOrder(
            version = SHOPIFY_API_VERSION,
            orderId = orderId,
        ).order

    override suspend fun createOrder(request: ShopifyOrderRequestDto): OrderDto =
        apiService.createOrder(
            version = SHOPIFY_API_VERSION,
            request = request,
        ).order

    override suspend fun cancelOrder(orderId: Long, request: ShopifyCancelOrderRequestDto): OrderDto =
        apiService.cancelOrder(
            version = SHOPIFY_API_VERSION,
            orderId = orderId,
            request = request,
        ).order

    private companion object {
        const val SHOPIFY_API_VERSION = "2024-04"
    }
}
