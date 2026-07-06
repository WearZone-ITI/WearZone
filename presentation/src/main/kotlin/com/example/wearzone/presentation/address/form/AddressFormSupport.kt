package com.example.wearzone.presentation.address.form

import com.example.presentation.R
import com.example.wearzone.domain.customer.address.model.AddressLookupTokenMissingException
import com.example.wearzone.domain.customer.address.model.AddressLookupUnavailableException
import com.example.wearzone.domain.customer.address.model.AddressNetworkException
import com.example.wearzone.domain.customer.address.model.AddressPermissionException
import com.example.wearzone.domain.customer.address.model.AddressResponseParseException
import com.example.wearzone.domain.customer.address.model.AddressSuggestion
import com.example.wearzone.domain.customer.address.model.Country
import com.example.wearzone.domain.customer.address.model.CurrentLocationPermissionDeniedException
import com.example.wearzone.domain.customer.address.model.CurrentLocationUnavailableException
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.model.InvalidShopifyAddressException
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerNotFoundException
import com.example.wearzone.domain.customer.address.model.AddressInput
import kotlinx.collections.immutable.ImmutableList

internal const val DEFAULT_COUNTRY_ISO_CODE = "EG"

internal val FALLBACK_COUNTRY = CountryUiModel(
    name = "Egypt",
    isoCode = "EG",
    dialCode = "+20",
    minNationalNumberLength = 10,
    maxNationalNumberLength = 10,
    exampleNationalNumber = "1012345678",
    trunkPrefix = "0",
)

internal fun AddressFormUiState.withValidation(showRequiredErrors: Boolean): AddressFormUiState {
    val country = selectedCountry ?: FALLBACK_COUNTRY
    return copy(
        recipientNameError = recipientNameError(recipientName, showRequiredErrors),
        phoneError = phoneError(country, phone, showRequiredErrors),
        address1Error = requiredTextError(address1, showRequiredErrors),
        cityError = requiredTextError(city, showRequiredErrors),
        countryError = countryError(countryIsoCode, showRequiredErrors),
        zipError = zipError(zip, showRequiredErrors),
    )
}

internal fun AddressFormUiState.toAddressInput(): AddressInput {
    val nameParts = recipientName.trim().split(NAME_SPLIT_REGEX, limit = 2)
    val country = selectedCountry ?: FALLBACK_COUNTRY
    return AddressInput(
        firstName = nameParts.firstOrNull().orEmpty(),
        lastName = nameParts.getOrNull(1).orEmpty(),
        company = company.toOptionalString(),
        address1 = address1.trim(),
        address2 = address2.toOptionalString(),
        city = city.trim(),
        province = province.toOptionalString(),
        country = country.name,
        zip = zip.trim(),
        phone = phone.toInternationalPhoneNumber(country),
    )
}

internal fun CustomerAddress.toFormState(
    countries: ImmutableList<CountryUiModel>,
): AddressFormUiState {
    val country = resolveCountry(countries)
    return AddressFormUiState(
        addressId = id,
        isLoading = false,
        countries = countries,
        recipientName = name?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(firstName, lastName).joinToString(" "),
        countryIsoCode = country.isoCode,
        countryDialCode = country.dialCode,
        phone = phone.orEmpty().toNationalPhoneInput(country),
        company = company.orEmpty(),
        address1 = address1.orEmpty(),
        address2 = address2.orEmpty(),
        city = city.orEmpty(),
        province = province.orEmpty(),
        country = country.name,
        zip = zip.orEmpty(),
        isDefault = isDefault,
    ).withValidation(showRequiredErrors = false)
}

internal fun AddressFormUiState.withDefaultCountry(): AddressFormUiState {
    val defaultCountry = countries.findCountry(DEFAULT_COUNTRY_ISO_CODE)
        ?: countries.firstOrNull()
        ?: FALLBACK_COUNTRY
    return copy(
        country = defaultCountry.name,
        countryIsoCode = defaultCountry.isoCode,
        countryDialCode = defaultCountry.dialCode,
        phone = phone.toNationalPhoneInput(defaultCountry),
    )
}

internal fun String.toNationalPhoneInput(country: CountryUiModel): String {
    val digits = filter(Char::isDigit)
    val dialDigits = country.dialCode.filter(Char::isDigit)
    val withoutDial = when {
        digits.startsWith(DOUBLE_ZERO_PREFIX + dialDigits) ->
            digits.removePrefix(DOUBLE_ZERO_PREFIX + dialDigits)
        digits.startsWith(dialDigits) && digits.length > country.maxNationalNumberLength ->
            digits.removePrefix(dialDigits)
        else -> digits
    }
    return country.trunkPrefix
        ?.takeIf { withoutDial.startsWith(it) && withoutDial.length > country.maxNationalNumberLength }
        ?.let { withoutDial.removePrefix(it) }
        ?: withoutDial
}

internal fun List<CountryUiModel>.findCountry(isoCode: String): CountryUiModel? =
    firstOrNull { it.isoCode.equals(isoCode, ignoreCase = true) }

internal fun Country.toUiModel(): CountryUiModel =
    CountryUiModel(
        name = name,
        isoCode = isoCode,
        dialCode = dialCode,
        minNationalNumberLength = minNationalNumberLength,
        maxNationalNumberLength = maxNationalNumberLength,
        exampleNationalNumber = exampleNationalNumber,
        trunkPrefix = trunkPrefix,
    )

internal fun AddressSuggestion.toUiModel(): AddressSuggestionUiModel =
    AddressSuggestionUiModel(
        id = id,
        title = title,
        subtitle = subtitle,
        address1 = address1,
        city = city,
        province = province,
        countryName = countryName,
        countryCode = countryCode,
        postalCode = postalCode,
        latitude = coordinates?.latitude,
        longitude = coordinates?.longitude,
    )

internal fun Throwable.toAddressMessageRes(): Int =
    when (this) {
        is ShopifyCustomerIdUnavailableException -> R.string.address_error_customer_id_unavailable
        is AddressPermissionException -> R.string.address_error_permission
        is ShopifyCustomerNotFoundException -> R.string.address_error_customer_not_found
        is InvalidShopifyAddressException -> R.string.address_error_invalid_shopify_address
        is AddressResponseParseException -> R.string.address_error_response_parse
        is AddressNetworkException -> R.string.address_error_network
        else -> R.string.address_error_generic
    }

internal fun Throwable.toLookupMessageRes(): Int =
    when (this) {
        is AddressLookupTokenMissingException -> R.string.address_mapbox_token_missing
        is CurrentLocationPermissionDeniedException -> R.string.address_location_permission_denied
        is CurrentLocationUnavailableException -> R.string.address_location_unavailable
        is AddressLookupUnavailableException -> R.string.address_lookup_unavailable
        else -> R.string.address_lookup_unavailable
    }

private fun recipientNameError(value: String, showRequiredErrors: Boolean): Int? =
    if (value.trim().isBlank()) requiredError(showRequiredErrors) else null

private fun phoneError(
    country: CountryUiModel,
    value: String,
    showRequiredErrors: Boolean,
): Int? {
    val nationalDigits = value.toNationalPhoneInput(country)
    return when {
        nationalDigits.isBlank() -> requiredError(showRequiredErrors)
        nationalDigits.length !in country.minNationalNumberLength..country.maxNationalNumberLength ->
            R.string.address_error_invalid_phone
        else -> null
    }
}

private fun requiredTextError(value: String, showRequiredErrors: Boolean): Int? =
    if (value.isBlank()) requiredError(showRequiredErrors) else null

private fun countryError(value: String, showRequiredErrors: Boolean): Int? =
    if (value.isBlank()) requiredError(showRequiredErrors) else null

private fun zipError(value: String, showRequiredErrors: Boolean): Int? =
    when {
        value.isBlank() -> null
        value.any { !it.isDigit() } -> R.string.address_error_invalid_postal_code
        else -> null
    }

private fun requiredError(showRequiredErrors: Boolean): Int? =
    if (showRequiredErrors) R.string.address_error_required_field else null

private fun CustomerAddress.resolveCountry(countries: ImmutableList<CountryUiModel>): CountryUiModel =
    countryCode?.let { countries.findCountry(it) }
        ?: countryName?.let { countries.findByName(it) }
        ?: country?.let { countries.findByName(it) }
        ?: phone?.let { countries.findByDialCode(it) }
        ?: countries.findCountry(DEFAULT_COUNTRY_ISO_CODE)
        ?: FALLBACK_COUNTRY

private fun String.toOptionalString(): String? = trim().takeIf { it.isNotBlank() }

private fun String.toInternationalPhoneNumber(country: CountryUiModel): String {
    val nationalDigits = toNationalPhoneInput(country)
    val dialDigits = country.dialCode.filter(Char::isDigit)
    return "+$dialDigits$nationalDigits"
}

private fun List<CountryUiModel>.findByName(name: String): CountryUiModel? =
    firstOrNull { it.name.equals(name, ignoreCase = true) }

private fun List<CountryUiModel>.findByDialCode(phone: String): CountryUiModel? {
    val digits = phone.filter(Char::isDigit)
    return sortedByDescending { it.dialCode.length }
        .firstOrNull { digits.startsWith(it.dialCode.filter(Char::isDigit)) }
}

private val NAME_SPLIT_REGEX = "\\s+".toRegex()
private const val DOUBLE_ZERO_PREFIX = "00"
