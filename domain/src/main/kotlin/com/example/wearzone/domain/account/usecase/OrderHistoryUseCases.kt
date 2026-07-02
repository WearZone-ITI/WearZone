package com.example.wearzone.domain.account.usecase

import com.example.wearzone.domain.customer.address.usecase.GetCurrentCustomerIdUseCase

data class OrderHistoryUseCases(
    val getCurrentCustomerId: GetCurrentCustomerIdUseCase,
    val getOrderHistory: GetOrderHistoryUseCase,
)
