package com.example.domain.auth.usecase

import com.example.domain.auth.model.User
import com.example.domain.auth.repository.IAuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        return authRepository.loginWithGoogleCredential(idToken)
    }
}
