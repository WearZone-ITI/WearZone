package com.example.wearzone.domain.customer.address.repository

interface ICustomerIdProvider {
    suspend fun getCurrentCustomerId(): Result<Long>
}
