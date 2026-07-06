package com.example.wearzone.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyResponseDto(
    @SerialName("result") val result: String,
    @SerialName("base_code") val baseCode: String,
    @SerialName("rates") val rates: Map<String, Double>
)
