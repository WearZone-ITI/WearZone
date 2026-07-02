package com.example.domain.checkout.usecase

import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import com.example.wearzone.domain.checkout.model.CheckoutDiscountValueType
import com.example.wearzone.domain.checkout.model.EmptyDiscountCodeException
import com.example.wearzone.domain.checkout.model.InvalidDiscountCodeException
import com.example.wearzone.domain.checkout.repository.IDiscountRepository
import com.example.wearzone.domain.checkout.usecase.ApplyDiscountCodeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyDiscountCodeUseCaseTest {

    @Test
    fun `invoke with empty code returns EmptyDiscountCodeException`() = runTest {
        val useCase = ApplyDiscountCodeUseCase(FakeDiscountRepository())

        val result = useCase("   ", SUBTOTAL)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EmptyDiscountCodeException)
    }

    @Test
    fun `invoke with valid percentage code calculates discount amount`() = runTest {
        val repository = FakeDiscountRepository(
            result = Result.success(
                discount(
                    code = VALID_CODE,
                    value = PERCENTAGE_VALUE,
                    valueType = CheckoutDiscountValueType.Percentage,
                ),
            ),
        )
        val useCase = ApplyDiscountCodeUseCase(repository)

        val result = useCase(VALID_CODE, SUBTOTAL)

        assertTrue(result.isSuccess)
        assertEquals(20.0, result.getOrNull()?.calculatedAmount)
    }

    @Test
    fun `invoke with valid fixed amount code calculates discount amount`() = runTest {
        val repository = FakeDiscountRepository(
            result = Result.success(
                discount(
                    code = VALID_CODE,
                    value = FIXED_VALUE,
                    valueType = CheckoutDiscountValueType.FixedAmount,
                ),
            ),
        )
        val useCase = ApplyDiscountCodeUseCase(repository)

        val result = useCase(VALID_CODE, SUBTOTAL)

        assertTrue(result.isSuccess)
        assertEquals(FIXED_VALUE, result.getOrNull()?.calculatedAmount)
    }

    @Test
    fun `invoke clamps discount so total cannot go below zero`() = runTest {
        val repository = FakeDiscountRepository(
            result = Result.success(
                discount(
                    code = VALID_CODE,
                    value = OVER_SUBTOTAL_VALUE,
                    valueType = CheckoutDiscountValueType.FixedAmount,
                ),
            ),
        )
        val useCase = ApplyDiscountCodeUseCase(repository)

        val result = useCase(VALID_CODE, SUBTOTAL)

        assertTrue(result.isSuccess)
        assertEquals(SUBTOTAL, result.getOrNull()?.calculatedAmount)
    }

    @Test
    fun `invoke with invalid repository result returns failure`() = runTest {
        val repository = FakeDiscountRepository(
            result = Result.failure(InvalidDiscountCodeException()),
        )
        val useCase = ApplyDiscountCodeUseCase(repository)

        val result = useCase(VALID_CODE, SUBTOTAL)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidDiscountCodeException)
    }

    @Test
    fun `invoke trims code before validation`() = runTest {
        val repository = FakeDiscountRepository(
            result = Result.success(
                discount(
                    code = VALID_CODE,
                    value = PERCENTAGE_VALUE,
                    valueType = CheckoutDiscountValueType.Percentage,
                ),
            ),
        )
        val useCase = ApplyDiscountCodeUseCase(repository)

        useCase("  $VALID_CODE  ", SUBTOTAL)

        assertEquals(VALID_CODE, repository.lastValidatedCode)
    }

    private class FakeDiscountRepository(
        private val result: Result<CheckoutDiscount> = Result.success(
            discount(
                code = VALID_CODE,
                value = PERCENTAGE_VALUE,
                valueType = CheckoutDiscountValueType.Percentage,
            ),
        ),
    ) : IDiscountRepository {
        var lastValidatedCode: String? = null
            private set

        override suspend fun validateDiscountCode(code: String): Result<CheckoutDiscount> {
            lastValidatedCode = code
            return result
        }
    }

    private companion object {
        const val VALID_CODE = "SUMMER10"
        const val SUBTOTAL = 100.0
        const val PERCENTAGE_VALUE = 20.0
        const val FIXED_VALUE = 15.0
        const val OVER_SUBTOTAL_VALUE = 150.0

        fun discount(
            code: String,
            value: Double,
            valueType: CheckoutDiscountValueType,
        ): CheckoutDiscount =
            CheckoutDiscount(
                code = code,
                value = value,
                valueType = valueType,
                calculatedAmount = 0.0,
            )
    }
}
