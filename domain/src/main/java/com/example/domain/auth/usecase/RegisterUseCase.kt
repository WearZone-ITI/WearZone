package com.example.domain.auth.usecase

import com.example.domain.auth.model.User
import com.example.domain.auth.repository.IAuthRepository
import com.example.domain.util.ValidationError
import com.example.domain.util.ValidationException
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: IAuthRepository,
) {
    private val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean,
    ): Result<User> {
        when {
            name.isBlank() -> return Result.failure(ValidationException(ValidationError.BlankName))

            !emailRegex.matches(email) -> return Result.failure(ValidationException(ValidationError.InvalidEmail))

            password.length < 8 -> return Result.failure(ValidationException(ValidationError.ShortPassword))

            password != confirmPassword -> return Result.failure(ValidationException(ValidationError.PasswordMismatch))

            !termsAccepted -> return Result.failure(ValidationException(ValidationError.TermsNotAccepted))
        }

        return authRepository.register(name.trim(), email.trim(), password)
    }
}
