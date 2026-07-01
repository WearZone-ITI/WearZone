package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.data.remote.datasource.IAuthRemoteDataSource
import com.example.wearzone.domain.common.runCatchingCancellable
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerIdUnavailableException
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CurrentCustomerIdProviderImpl @Inject constructor(
    private val settingsPreferencesDataSource: ISettingsPreferencesDataSource,
    private val authRemoteDataSource: IAuthRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICustomerIdProvider {
    override suspend fun getCurrentCustomerId(): Result<Long> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val shopifyCustomerId = settingsPreferencesDataSource.getCustomerId()
                if (shopifyCustomerId != null && shopifyCustomerId > 0L) {
                    return@runCatchingCancellable shopifyCustomerId
                }

                val firebaseUser = authRemoteDataSource.getCurrentUser()
                    ?: throw ShopifyCustomerIdUnavailableException()
                val savedCustomerId = authRemoteDataSource
                    .getSavedShopifyCustomerId(firebaseUser.uid)
                    ?.takeIf { it > 0L }
                    ?: throw ShopifyCustomerIdUnavailableException()

                settingsPreferencesDataSource.setCustomerId(savedCustomerId)
                savedCustomerId
            }
        }
}
