package com.example.wearzone.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.model.AddressSuggestion
import com.example.wearzone.domain.customer.address.repository.IAddressLookupRepository

class ReverseGeocodeAddressUseCase(
    private val repository: IAddressLookupRepository,
) {
    suspend operator fun invoke(coordinates: AddressCoordinates): Result<AddressSuggestion?> =
        repository.reverseGeocode(coordinates)
}
