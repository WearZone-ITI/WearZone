package com.example.wearzone.domain.customer.address.repository

import com.example.wearzone.domain.customer.address.model.AddressCoordinates

interface ICurrentLocationRepository {
    suspend fun getCurrentCoordinates(): Result<AddressCoordinates>
}
