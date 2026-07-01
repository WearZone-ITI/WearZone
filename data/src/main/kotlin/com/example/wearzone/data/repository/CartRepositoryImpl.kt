package com.example.wearzone.data.repository

import android.util.Log
import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.local.datasource.ICartLocalDataSource
import com.example.wearzone.data.local.datasource.ISettingsPreferencesDataSource
import com.example.wearzone.data.local.entity.toDomain
import com.example.wearzone.data.local.entity.toEntity
import com.example.wearzone.data.remote.datasource.ICartRemoteDataSource
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
import kotlinx.coroutines.flow.flowOn
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
    private val settingsDataSource: ISettingsPreferencesDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICartRepository {

    companion object { private const val TAG = "CartRepository" }
    private val syncScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val syncMutex = Mutex()

    override fun observeCart(): Flow<List<CartItem>> =
        localDataSource
            .observeCartItems()
            .map { list ->
                list.map { it.toDomain() }
            }

    override suspend fun addToCart(
        item: CartItem
    ): DataResult<Unit> =
        executeCartOperation {
            localDataSource.addOrUpdateItem(
                item.toEntity()
            )
        }

    override suspend fun removeFromCart(
        variantId: String
    ): DataResult<Unit> =
        executeCartOperation {
            localDataSource.removeItem(
                variantId
            )
        }

    override suspend fun updateQuantity(
        variantId: String,
        quantity: Int
    ): DataResult<Unit> =
        executeCartOperation {
            localDataSource.updateQuantity(
                variantId,
                quantity
            )
        }

    override suspend fun clearCart(): DataResult<Unit> =
        executeCartOperation {
            localDataSource.clearCart()
        }

    private suspend fun executeCartOperation(
        action: suspend () -> Unit
    ): DataResult<Unit> =
        withContext(ioDispatcher) {

            runCatchingCancellable {
                action()
            }.fold(
                onSuccess = {
                    // Don't await remote sync - local update is instant
                    // Remote sync happens in background
                    launchRemoteSync()
                    DataResult.Success(Unit)
                },
                onFailure = {
                    Log.e(
                        TAG,
                        "Cart operation failed",
                        it
                    )

                    DataResult.Error(
                        DomainError.Unknown(it)
                    )
                }
            )
        }

    private fun launchRemoteSync() {

        syncScope.launch {

            try {

                syncCartWithRemote()

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Remote sync failed",
                    e
                )
            }
        }
    }

    private suspend fun syncCartWithRemote() {

        syncMutex.withLock {

            val currentItems =
                localDataSource
                    .observeCartItems()
                    .firstOrNull()
                    ?: emptyList()

            val draftOrderId =
                settingsDataSource
                    .observeDraftOrderId()
                    .firstOrNull()

            Log.d(
                TAG,
                "Current items = $currentItems"
            )

            if (
                currentItems.isEmpty() &&
                draftOrderId == null
            ) {
                return
            }

            if (currentItems.isEmpty()) {

                if (draftOrderId != null) {

                    remoteDataSource
                        .deleteDraftOrder(
                            draftOrderId
                        )

                    settingsDataSource
                        .setDraftOrderId(
                            null
                        )
                }

                return
            }

            val lineItems =
                currentItems
                    .mapNotNull { item ->

                        val variantId =
                            item.variantId
                                .toLongOrNull()

                        val productId =
                            item.productId
                                .toLongOrNull()

                        if (
                            variantId == null ||
                            productId == null
                        ) {
                            return@mapNotNull null
                        }

                        DraftOrderLineItem(
                            variantId = variantId,
                            quantity = item.quantity
                        )
                    }

            if (lineItems.isEmpty()) {
                return
            }

            val request =
                DraftOrderRequest(
                    draftOrder =
                        DraftOrderPayload(
                            lineItems = lineItems,
                            useCustomerDefaultAddress = true
                        )
                )

            if (draftOrderId != null) {

                try {

                    remoteDataSource
                        .updateDraftOrder(
                            draftOrderId,
                            request
                        )

                    Log.d(
                        TAG,
                        "Draft updated"
                    )

                } catch (e: HttpException) {

                    val error =
                        e.response()
                            ?.errorBody()
                            ?.string()
                            .orEmpty()

                    Log.e(
                        TAG,
                        error
                    )

                    if (
                        e.code() == 422 &&
                        error.contains(
                            "no longer available"
                        )
                    ) {

                        val unavailableProducts =
                            Regex(
                                """Product with ID (\d+)"""
                            )
                                .findAll(error)
                                .map {
                                    it.groupValues[1]
                                }
                                .toSet()

                        currentItems
                            .filter {
                                it.productId in unavailableProducts
                            }
                            .forEach {

                                localDataSource
                                    .removeItem(
                                        it.variantId
                                    )
                            }

                        syncCartWithRemote()
                    }
                }

            } else {

                try {

                    val response =
                        remoteDataSource
                            .createDraftOrder(
                                request
                            )

                    settingsDataSource
                        .setDraftOrderId(
                            response.draftOrder.id
                        )

                } catch (e: HttpException) {

                    val error =
                        e.response()
                            ?.errorBody()
                            ?.string()
                            .orEmpty()

                    Log.e(
                        TAG,
                        "Create draft failed: $error",
                        e
                    )

                } catch (e: Exception) {

                    Log.e(
                        TAG,
                        "Create draft failed",
                        e
                    )
                }
            }
        }
    }
}