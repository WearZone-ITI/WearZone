package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ICartLocalDataSource
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.data.local.entity.toDomain
import com.example.wearzone.data.local.entity.toEntity
import com.example.wearzone.data.remote.datasource.ICartRemoteDataSource
import com.example.wearzone.data.remote.datasource.IAuthRemoteDataSource
import com.example.wearzone.data.remote.dto.DraftOrderCustomer
import com.example.wearzone.data.remote.dto.DraftOrderLineItem
import com.example.wearzone.data.remote.dto.DraftOrderPayload
import com.example.wearzone.data.remote.dto.DraftOrderRequest
import com.example.wearzone.domain.cart.model.CartItem
import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.common.DomainError
import com.example.wearzone.domain.common.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val localDataSource: ICartLocalDataSource,
    private val remoteDataSource: ICartRemoteDataSource,
    private val authRemoteDataSource: IAuthRemoteDataSource,
    private val settingsDataSource: ISettingsPreferencesDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICartRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val syncMutex = Mutex()

    override fun observeCart(): Flow<List<CartItem>> =
        localDataSource.observeCartItems().map { list ->
                list.map { it.toDomain() }
            }

    override suspend fun addToCart(
        item: CartItem
    ): DataResult<Unit> = executeCartOperation {
        localDataSource.addOrUpdateItem(
            item.toEntity()
        )
    }

    override suspend fun removeFromCart(
        variantId: String
    ): DataResult<Unit> = executeCartOperation {
        localDataSource.removeItem(
            variantId
        )
    }

    override suspend fun updateQuantity(
        variantId: String, quantity: Int
    ): DataResult<Unit> = executeCartOperation {
        localDataSource.updateQuantity(
            variantId, quantity
        )
    }

    override suspend fun clearCart(): DataResult<Unit> = executeCartOperation {
        localDataSource.clearCart()
    }

    private suspend fun executeCartOperation(
        action: suspend () -> Unit
    ): DataResult<Unit> = withContext(ioDispatcher) {

        runCatchingCancellable {
            action()
        }.fold(onSuccess = {
            launchRemoteSync()
            DataResult.Success(Unit)
        }, onFailure = {
            DataResult.Error(
                DomainError.Unknown(it)
            )
        })
    }

    private fun launchRemoteSync() {
        syncScope.launch {
            try {
                syncCartWithRemote()
            } catch (e: Exception) {
                DomainError.Network(e)
            }
        }
    }

    private suspend fun syncCartWithRemote() {

        syncMutex.withLock {
            if (authRemoteDataSource.getCurrentUser() == null) {
                return
            }

            val customerId = settingsDataSource.getCustomerId()
            if (customerId == null || customerId <= 0L) {
                return
            }

            val currentItems = localDataSource.observeCartItems().firstOrNull() ?: emptyList()

            val draftOrderId = settingsDataSource.observeDraftOrderId().firstOrNull()

            if (currentItems.isEmpty() && draftOrderId == null) {
                return
            }

            if (currentItems.isEmpty()) {

                if (draftOrderId != null) {

                    try {
                        remoteDataSource.deleteDraftOrder(
                                draftOrderId
                            )

                        settingsDataSource.setDraftOrderId(
                                null
                            )
                    } catch (e: Exception) {
                        DomainError.Network(e)
                    }
                }

                return
            }

            val lineItems = currentItems.mapNotNull { item ->

                    val variantId = item.variantId.toLongOrNull()
                        ?: return@mapNotNull null

                    DraftOrderLineItem(
                        variantId = variantId,
                        quantity = item.quantity.coerceIn(1, item.maxQuantity.coerceAtLeast(1))
                    )
                }

            if (lineItems.isEmpty()) {
                return
            }

            val request = DraftOrderRequest(
                draftOrder = DraftOrderPayload(
                    lineItems = lineItems,
                    customer = DraftOrderCustomer(customerId),
                    useCustomerDefaultAddress = true
                )
            )

            if (draftOrderId != null) {
                try {
                    remoteDataSource.updateDraftOrder(
                            draftOrderId, request
                        )

                } catch (e: HttpException) {

                    val error = e.response()?.errorBody()?.string().orEmpty()

                    if (e.code() == 422 && error.contains(
                            "no longer available"
                        )
                    ) {

                        val unavailableProducts = Regex(
                            """Product with ID (\d+)"""
                        ).findAll(error).map {
                                it.groupValues[1]
                            }.toSet()

                        currentItems.filter {
                                it.productId in unavailableProducts
                            }.forEach {

                                localDataSource.removeItem(
                                        it.variantId
                                    )
                            }

                        syncCartWithRemote()
                    } else {
                        DomainError.Network(Exception("Failed to update draft order: HTTP ${e.code()} - $error"))
                    }
                } catch (e: Exception) {
                    DomainError.Network(Exception("Failed to update draft order"))
                }

            } else {

                try {
                    val response = remoteDataSource.createDraftOrder(
                            request
                        )

                    settingsDataSource.setDraftOrderId(
                            response.draftOrder.id
                        )

                } catch (e: HttpException) {

                    val error = e.response()?.errorBody()?.string().orEmpty()
                    DomainError.Network(Exception("Failed to create draft order: HTTP ${e.code()} - $error"))

                } catch (e: Exception) {
                    DomainError.Network(Exception("Failed to create draft order"))
                }
            }
        }
    }
}
