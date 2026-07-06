package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MapboxGeocodingResponseDto(
    val features: List<MapboxFeatureDto> = emptyList(),
)

@Serializable
data class MapboxFeatureDto(
    val id: String = "",
    val text: String = "",
    val address: String? = null,
    @SerialName("place_name")
    val placeName: String = "",
    val center: List<Double> = emptyList(),
    val context: List<MapboxContextDto> = emptyList(),
    val properties: MapboxPropertiesDto = MapboxPropertiesDto(),
)

@Serializable
data class MapboxContextDto(
    val id: String = "",
    val text: String = "",
    @SerialName("short_code")
    val shortCode: String? = null,
)

@Serializable
data class MapboxPropertiesDto(
    @SerialName("short_code")
    val shortCode: String? = null,
)
