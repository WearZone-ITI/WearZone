package com.example.wearzone.domain.customer.address.usecase

data class CustomerAddressUseCases(
    val getCurrentCustomerId: GetCurrentCustomerIdUseCase,
    val getAddresses: GetCustomerAddressesUseCase,
    val getAddress: GetCustomerAddressUseCase,
    val createAddress: CreateCustomerAddressUseCase,
    val updateAddress: UpdateCustomerAddressUseCase,
    val setDefaultAddress: SetDefaultCustomerAddressUseCase,
    val deleteAddress: DeleteCustomerAddressUseCase,
)
