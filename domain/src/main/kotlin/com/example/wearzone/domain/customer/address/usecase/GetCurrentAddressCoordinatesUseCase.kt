package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.repository.ICurrentLocationRepository

class GetCurrentAddressCoordinatesUseCase(
    private val repository: ICurrentLocationRepository,
) {
    suspend operator fun invoke(): Result<AddressCoordinates> =
        repository.getCurrentCoordinates()
}
