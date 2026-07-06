package com.example.domain.customer.address.usecase

import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.model.AddressSuggestion
import com.example.wearzone.domain.customer.address.model.Country
import com.example.wearzone.domain.customer.address.repository.IAddressLookupRepository
import com.example.wearzone.domain.customer.address.repository.ICountryRepository
import com.example.wearzone.domain.customer.address.repository.ICurrentLocationRepository
import com.example.wearzone.domain.customer.address.usecase.GetCountriesUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCurrentAddressCoordinatesUseCase
import com.example.wearzone.domain.customer.address.usecase.ReverseGeocodeAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.SearchAddressSuggestionsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressLookupUseCasesTest {

    @Test
    fun `get countries delegates to repository`() = runTest {
        val repository = FakeCountryRepository()
        val useCase = GetCountriesUseCase(repository)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals("EG", result.getOrNull()?.first()?.isoCode)
    }

    @Test
    fun `search address suggestions delegates query and country`() = runTest {
        val repository = FakeAddressLookupRepository()
        val useCase = SearchAddressSuggestionsUseCase(repository)

        val result = useCase("Cairo Festival", "EG")

        assertTrue(result.isSuccess)
        assertEquals("Cairo Festival", repository.lastQuery)
        assertEquals("EG", repository.lastCountryIsoCode)
    }

    @Test
    fun `reverse geocode delegates coordinates`() = runTest {
        val repository = FakeAddressLookupRepository()
        val useCase = ReverseGeocodeAddressUseCase(repository)
        val coordinates = AddressCoordinates(latitude = 30.0444, longitude = 31.2357)

        val result = useCase(coordinates)

        assertTrue(result.isSuccess)
        assertEquals(coordinates, repository.lastReverseCoordinates)
    }

    @Test
    fun `get current coordinates delegates to repository`() = runTest {
        val repository = FakeCurrentLocationRepository()
        val useCase = GetCurrentAddressCoordinatesUseCase(repository)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(30.0444, result.getOrNull()?.latitude)
    }

    private class FakeCountryRepository : ICountryRepository {
        override suspend fun getCountries(): Result<List<Country>> =
            Result.success(
                listOf(
                    Country("Egypt", "EG", "+20", 10, 10, "1012345678"),
                )
            )
    }

    private class FakeAddressLookupRepository : IAddressLookupRepository {
        var lastQuery: String? = null
            private set
        var lastCountryIsoCode: String? = null
            private set
        var lastReverseCoordinates: AddressCoordinates? = null
            private set

        override suspend fun searchAddressSuggestions(
            query: String,
            countryIsoCode: String?,
        ): Result<List<AddressSuggestion>> {
            lastQuery = query
            lastCountryIsoCode = countryIsoCode
            return Result.success(listOf(suggestion))
        }

        override suspend fun reverseGeocode(
            coordinates: AddressCoordinates,
        ): Result<AddressSuggestion?> {
            lastReverseCoordinates = coordinates
            return Result.success(suggestion)
        }
    }

    private class FakeCurrentLocationRepository : ICurrentLocationRepository {
        override suspend fun getCurrentCoordinates(): Result<AddressCoordinates> =
            Result.success(AddressCoordinates(latitude = 30.0444, longitude = 31.2357))
    }

    private companion object {
        val suggestion = AddressSuggestion(
            id = "mapbox.1",
            title = "Cairo Festival City",
            subtitle = "Cairo Festival City, Cairo, Egypt",
            address1 = "Cairo Festival City",
            city = "Cairo",
            province = "Cairo Governorate",
            countryName = "Egypt",
            countryCode = "EG",
            postalCode = "11835",
            coordinates = AddressCoordinates(latitude = 30.0444, longitude = 31.2357),
        )
    }
}
