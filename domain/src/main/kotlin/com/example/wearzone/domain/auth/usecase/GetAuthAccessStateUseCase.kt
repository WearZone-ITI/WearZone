package com.example.wearzone.domain.auth.usecase

import com.example.wearzone.domain.auth.model.AuthAccessState
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider

class GetAuthAccessStateUseCase(
    private val authRepository: IAuthRepository,
    private val customerIdProvider: ICustomerIdProvider,
) {
    suspend operator fun invoke(): AuthAccessState {
        val user = authRepository.getCurrentUser() ?: return AuthAccessState.Guest
        if (user.uid.isBlank()) return AuthAccessState.Guest

        return customerIdProvider.getCurrentCustomerId()
            .fold(
                onSuccess = { customerId ->
                    AuthAccessState.AuthenticatedCustomer(customerId)
                },
                onFailure = {
                    AuthAccessState.AuthenticatedMissingCustomerId
                },
            )
    }
}
