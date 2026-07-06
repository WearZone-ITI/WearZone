package com.example.wearzone.data.local.datasource

import com.example.wearzone.domain.customer.address.model.Country
import javax.inject.Inject

class StaticCountryLocalDataSourceImpl @Inject constructor() : ICountryLocalDataSource {
    override fun getCountries(): List<Country> = COUNTRIES

    private companion object {
        val COUNTRIES = listOf(
            Country("Egypt", "EG", "+20", 10, 10, "1012345678"),
            Country("Saudi Arabia", "SA", "+966", 9, 9, "512345678"),
            Country("United Arab Emirates", "AE", "+971", 9, 9, "501234567"),
            Country("Kuwait", "KW", "+965", 8, 8, "51234567", trunkPrefix = null),
            Country("Qatar", "QA", "+974", 8, 8, "33123456", trunkPrefix = null),
            Country("Bahrain", "BH", "+973", 8, 8, "36123456", trunkPrefix = null),
            Country("Oman", "OM", "+968", 8, 8, "91234567", trunkPrefix = null),
            Country("Jordan", "JO", "+962", 9, 9, "791234567"),
            Country("Lebanon", "LB", "+961", 7, 8, "71123456"),
            Country("Morocco", "MA", "+212", 9, 9, "612345678"),
            Country("Tunisia", "TN", "+216", 8, 8, "20123456", trunkPrefix = null),
            Country("Algeria", "DZ", "+213", 9, 9, "551234567"),
            Country("Turkey", "TR", "+90", 10, 10, "5012345678"),
            Country("United States", "US", "+1", 10, 10, "2125550100", trunkPrefix = null),
            Country("Canada", "CA", "+1", 10, 10, "4165550100", trunkPrefix = null),
            Country("United Kingdom", "GB", "+44", 10, 10, "7123456789"),
            Country("France", "FR", "+33", 9, 9, "612345678"),
            Country("Germany", "DE", "+49", 10, 11, "15123456789"),
            Country("Italy", "IT", "+39", 9, 10, "3123456789", trunkPrefix = null),
            Country("Spain", "ES", "+34", 9, 9, "612345678", trunkPrefix = null),
        )
    }
}
