package com.example.wearzone.presentation.checkout.validation

import com.example.presentation.R
import com.example.wearzone.presentation.checkout.CardInfoUiModel

object CardValidator {
    fun validateCard(card: CardInfoUiModel): CardInfoUiModel {
        return card.copy(
            numberError = when {
                card.number.isBlank() -> R.string.card_number_required
                card.number.filter(Char::isDigit).length != 16 -> R.string.card_number_invalid
                else -> null
            },

            firstNameError = if (card.firstName.isBlank())
                R.string.first_name_required
            else null,

            lastNameError = if (card.lastName.isBlank())
                R.string.last_name_required
            else null,

            monthError = when {
                card.month.toIntOrNull() == null -> R.string.invalid_month
                card.month.toInt() !in 1..12 -> R.string.invalid_month
                else -> null
            },

            yearError = when {
                card.year.length != 4 -> R.string.invalid_year
                else -> null
            },

            cvvError = when {
                card.cvv.length !in 3..4 -> R.string.invalid_cvv
                else -> null
            }
        )
    }
}