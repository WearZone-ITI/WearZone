package com.example.wearzone.data.repository

import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.remote.datasource.ICustomerAddressRemoteDataSource
import com.example.wearzone.data.remote.dto.CustomerAddressDto
import com.example.wearzone.data.remote.dto.CustomerAddressInputDto
import com.example.wearzone.data.remote.dto.CustomerAddressRequestDto
import com.example.wearzone.domain.common.runCatchingCancellable
import com.example.wearzone.domain.customer.address.model.AddressInput
import com.example.wearzone.domain.customer.address.model.AddressNetworkException
import com.example.wearzone.domain.customer.address.model.AddressPermissionException
import com.example.wearzone.domain.customer.address.model.AddressResponseParseException
import com.example.wearzone.domain.customer.address.model.CannotDeleteDefaultAddressException
import com.example.wearzone.domain.customer.address.model.CustomerAddress
import com.example.wearzone.domain.customer.address.model.InvalidShopifyAddressException
import com.example.wearzone.domain.customer.address.model.ShopifyCustomerNotFoundException
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import javax.inject.Inject

class CustomerAddressRepositoryImpl @Inject constructor(
    private val remoteDataSource: ICustomerAddressRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ICustomerAddressRepository {

    override suspend fun getAddresses(customerId: Long): Result<List<CustomerAddress>> =
        runAddressOperation {
            remoteDataSource.getAddresses(customerId).map { it.toDomain() }
        }

    override suspend fun getAddress(customerId: Long, addressId: Long): Result<CustomerAddress> =
        runAddressOperation {
            remoteDataSource.getAddress(customerId, addressId).toDomain()
        }

    override suspend fun createAddress(
        customerId: Long,
        input: AddressInput,
    ): Result<CustomerAddress> =
        runAddressOperation {
            remoteDataSource.createAddress(customerId, input.toRequestDto()).toDomain()
        }

    override suspend fun updateAddress(
        customerId: Long,
        addressId: Long,
        input: AddressInput,
    ): Result<CustomerAddress> =
        runAddressOperation {
            remoteDataSource.updateAddress(customerId, addressId, input.toRequestDto()).toDomain()
        }

    override suspend fun setDefaultAddress(
        customerId: Long,
        addressId: Long,
    ): Result<CustomerAddress> =
        runAddressOperation {
            remoteDataSource.setDefaultAddress(customerId, addressId).toDomain()
        }

    override suspend fun deleteAddress(customerId: Long, addressId: Long): Result<Unit> =
        runAddressOperation {
            remoteDataSource.deleteAddress(customerId, addressId)
        }

    private suspend fun <T> runAddressOperation(block: suspend () -> T): Result<T> =
        withContext(ioDispatcher) {
            runCatchingCancellable { block() }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it.toAddressException()) },
            )
        }

    private fun Throwable.toAddressException(): Throwable =
        when (this) {
            is CannotDeleteDefaultAddressException -> this
            is HttpException -> toAddressHttpException()
            is IOException -> AddressNetworkException()
            is SerializationException -> AddressResponseParseException()
            else -> this
        }

    private fun HttpException.toAddressHttpException(): Throwable =
        when (code()) {
            401, 403 -> AddressPermissionException()
            404 -> ShopifyCustomerNotFoundException()
            400, 422 -> InvalidShopifyAddressException()
            408, 429, in 500..599 -> AddressNetworkException()
            else -> this
        }

    private fun CustomerAddressDto.toDomain(): CustomerAddress =
        CustomerAddress(
            id = id,
            customerId = customerId,
            firstName = firstName,
            lastName = lastName,
            company = company,
            address1 = address1,
            address2 = address2,
            city = city,
            province = province,
            country = country,
            zip = zip,
            phone = phone,
            name = name,
            provinceCode = provinceCode,
            countryCode = countryCode,
            countryName = countryName,
            isDefault = isDefault,
        )

    private fun AddressInput.toRequestDto(): CustomerAddressRequestDto =
        CustomerAddressRequestDto(
            address = CustomerAddressInputDto(
                firstName = firstName,
                lastName = lastName,
                company = company,
                address1 = address1,
                address2 = address2,
                city = city,
                province = province,
                country = country,
                zip = zip,
                phone = phone,
            )
        )
}
