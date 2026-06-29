package com.example.wearzone.domain.auth.usecase

import com.example.wearzone.domain.auth.repository.IAuthRepository

class GetCurrentUserUseCase(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke() = repository.getCurrentUser()
}