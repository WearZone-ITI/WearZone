package com.example.wearzone.domain.checkout.usecase

import com.example.wearzone.domain.checkout.model.CheckoutDiscount
import com.example.wearzone.domain.checkout.model.CheckoutDiscountValueType
import com.example.wearzone.domain.checkout.model.EmptyDiscountCodeException
import com.example.wearzone.domain.checkout.repository.IDiscountRepository
import kotlin.math.min

class ApplyDiscountCodeUseCase(
    private val discountRepository: IDiscountRepository,
) {
    suspend operator fun invoke(code: String, subtotal: Double): Result<CheckoutDiscount> {
        val trimmedCode = code.trim()
        if (trimmedCode.isEmpty()) {
            return Result.failure(EmptyDiscountCodeException())
        }

        return discountRepository.validateDiscountCode(trimmedCode)
            .map { discount ->
                val calculatedAmount = calculateDiscountAmount(
                    subtotal = subtotal,
                    value = discount.value,
                    valueType = discount.valueType,
                )
                discount.copy(calculatedAmount = calculatedAmount)
            }
    }

    private fun calculateDiscountAmount(
        subtotal: Double,
        value: Double,
        valueType: CheckoutDiscountValueType,
    ): Double {
        val rawAmount = when (valueType) {
            CheckoutDiscountValueType.Percentage -> subtotal * value / PERCENTAGE_DIVISOR
            CheckoutDiscountValueType.FixedAmount -> value
        }
        return min(rawAmount, subtotal).coerceAtLeast(MIN_DISCOUNT_AMOUNT)
    }

    private companion object {
        const val PERCENTAGE_DIVISOR = 100.0
        const val MIN_DISCOUNT_AMOUNT = 0.0
    }
}
