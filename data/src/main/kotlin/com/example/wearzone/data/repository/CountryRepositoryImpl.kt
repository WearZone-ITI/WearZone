package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ICountryLocalDataSource
import com.example.wearzone.domain.common.runCatchingCancellable
import com.example.wearzone.domain.customer.address.model.Country
import com.example.wearzone.domain.customer.address.repository.ICountryRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CountryRepositoryImpl @Inject constructor(
    private val localDataSource: ICountryLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICountryRepository {
    override suspend fun getCountries(): Result<List<Country>> =
        withContext(ioDispatcher) {
            runCatchingCancellable { localDataSource.getCountries() }
        }
}
