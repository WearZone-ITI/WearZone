package com.example.wearzone.domain.checkout.repository

import com.example.wearzone.domain.checkout.model.CheckoutOrder
import com.example.wearzone.domain.checkout.model.CheckoutOrderRequest

interface ICheckoutRepository {
    suspend fun createOrder(request: CheckoutOrderRequest): Result<CheckoutOrder>
}
