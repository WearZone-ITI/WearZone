package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.di.MapboxAccessToken
import com.example.wearzone.data.remote.datasource.IAddressLookupRemoteDataSource
import com.example.wearzone.data.remote.dto.MapboxContextDto
import com.example.wearzone.data.remote.dto.MapboxFeatureDto
import com.example.wearzone.domain.common.runCatchingCancellable
import com.example.wearzone.domain.customer.address.model.AddressCoordinates
import com.example.wearzone.domain.customer.address.model.AddressLookupTokenMissingException
import com.example.wearzone.domain.customer.address.model.AddressLookupUnavailableException
import com.example.wearzone.domain.customer.address.model.AddressSuggestion
import com.example.wearzone.domain.customer.address.repository.IAddressLookupRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class AddressLookupRepositoryImpl @Inject constructor(
    private val remoteDataSource: IAddressLookupRemoteDataSource,
    @MapboxAccessToken private val accessToken: String,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IAddressLookupRepository {
    override suspend fun searchAddressSuggestions(
        query: String,
        countryIsoCode: String?,
    ): Result<List<AddressSuggestion>> =
        runLookupOperation {
            if (query.trim().length < MIN_QUERY_LENGTH) return@runLookupOperation emptyList()
            remoteDataSource.searchAddressSuggestions(
                query = query.trim(),
                accessToken = requirePublicToken(),
                countryIsoCode = countryIsoCode,
            ).map { it.toDomain() }
        }

    override suspend fun reverseGeocode(coordinates: AddressCoordinates): Result<AddressSuggestion?> =
        runLookupOperation {
            remoteDataSource.reverseGeocode(
                longitude = coordinates.longitude,
                latitude = coordinates.latitude,
                accessToken = requirePublicToken(),
            )?.toDomain()
        }

    private suspend fun <T> runLookupOperation(block: suspend () -> T): Result<T> =
        withContext(ioDispatcher) {
            runCatchingCancellable { block() }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toLookupException()) },
            )
        }

    private fun requirePublicToken(): String {
        if (accessToken.isBlank() || !accessToken.startsWith(PUBLIC_TOKEN_PREFIX)) {
            throw AddressLookupTokenMissingException()
        }
        return accessToken
    }

    private fun Throwable.toLookupException(): Throwable =
        when (this) {
            is AddressLookupTokenMissingException -> this
            is HttpException,
            is IOException,
            is SerializationException,
                -> AddressLookupUnavailableException()
            else -> this
        }

    private fun MapboxFeatureDto.toDomain(): AddressSuggestion {
        val countryContext = context.firstContext(COUNTRY_CONTEXT)
        val regionContext = context.firstContext(REGION_CONTEXT)
        val placeContext = context.firstContext(PLACE_CONTEXT)
            ?: context.firstContext(LOCALITY_CONTEXT)
            ?: context.firstContext(DISTRICT_CONTEXT)
        val postcodeContext = context.firstContext(POSTCODE_CONTEXT)
        val featureType = id.substringBefore(FEATURE_ID_SEPARATOR)
        val countryCode = countryContext?.shortCode
            ?.substringBefore("-")
            ?.uppercase()
            ?: properties.shortCode?.substringBefore("-")?.uppercase()
        val title = buildAddressTitle()

        return AddressSuggestion(
            id = id.ifBlank { placeName },
            title = title,
            subtitle = placeName,
            address1 = title.ifBlank { placeName },
            city = when (featureType) {
                PLACE_CONTEXT, LOCALITY_CONTEXT -> text.takeIf { it.isNotBlank() }
                else -> placeContext?.text
            },
            province = if (featureType == REGION_CONTEXT) {
                text.takeIf { it.isNotBlank() }
            } else {
                regionContext?.text
            },
            countryName = countryContext?.text,
            countryCode = countryCode,
            postalCode = postcodeContext?.text,
            coordinates = center.toCoordinatesOrNull(),
        )
    }

    private fun MapboxFeatureDto.buildAddressTitle(): String =
        listOfNotNull(address, text)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { text.ifBlank { placeName.substringBefore(",") } }

    private fun List<MapboxContextDto>.firstContext(prefix: String): MapboxContextDto? =
        firstOrNull { it.id.startsWith(prefix) }

    private fun List<Double>.toCoordinatesOrNull(): AddressCoordinates? =
        if (size >= COORDINATE_COMPONENT_COUNT) {
            AddressCoordinates(latitude = this[1], longitude = this[0])
        } else {
            null
        }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
        const val PUBLIC_TOKEN_PREFIX = "pk."
        const val COORDINATE_COMPONENT_COUNT = 2
        const val COUNTRY_CONTEXT = "country"
        const val REGION_CONTEXT = "region"
        const val PLACE_CONTEXT = "place"
        const val LOCALITY_CONTEXT = "locality"
        const val DISTRICT_CONTEXT = "district"
        const val POSTCODE_CONTEXT = "postcode"
        const val FEATURE_ID_SEPARATOR = "."
    }
}
