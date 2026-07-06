package com.example.wearzone.data.local.datasource

import com.example.wearzone.domain.customer.address.model.AddressCoordinates

interface ICurrentLocationDataSource {
    suspend fun getCurrentCoordinates(): AddressCoordinates
}
