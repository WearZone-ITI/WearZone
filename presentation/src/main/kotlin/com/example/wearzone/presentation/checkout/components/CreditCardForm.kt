package com.example.wearzone.presentation.checkout.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.checkout.CardInfoUiModel
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun CreditCardForm(
    cardInfo: CardInfoUiModel,
    onNumberChange: (String) -> Unit,
    onNameChange: (String, String) -> Unit,
    onExpiryChange: (String, String) -> Unit,
    onCvvChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.checkout_card_details_title),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
            )

            CardTextField(
                value = cardInfo.number,
                onValueChange = onNumberChange,
                label = stringResource(R.string.checkout_card_number),
                placeholder = "0000 0000 0000 0000",
                keyboardType = KeyboardType.Number,
                errorRes = cardInfo.numberError,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTextField(
                    value = cardInfo.firstName,
                    onValueChange = { onNameChange(it, cardInfo.lastName) },
                    label = stringResource(R.string.checkout_card_first_name),
                    placeholder = "John",
                    modifier = Modifier.weight(1f),
                    errorRes = cardInfo.firstNameError
                )
                CardTextField(
                    value = cardInfo.lastName,
                    onValueChange = { onNameChange(cardInfo.firstName, it) },
                    label = stringResource(R.string.checkout_card_last_name),
                    placeholder = "Doe",
                    modifier = Modifier.weight(1f),
                    errorRes = cardInfo.lastNameError
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTextField(
                    value = cardInfo.month,
                    onValueChange = { onExpiryChange(it, cardInfo.year) },
                    label = stringResource(R.string.checkout_card_expiry_month),
                    placeholder = "MM",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    errorRes = cardInfo.monthError
                )
                CardTextField(
                    value = cardInfo.year,
                    onValueChange = { onExpiryChange(cardInfo.month, it) },
                    label = stringResource(R.string.checkout_card_expiry_year),
                    placeholder = "YYYY",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    errorRes = cardInfo.yearError
                )
                CardTextField(
                    value = cardInfo.cvv,
                    onValueChange = onCvvChange,
                    label = stringResource(R.string.checkout_card_cvv),
                    placeholder = "123",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    errorRes = cardInfo.cvvError
                )
            }
        }
    }
}

@Composable
private fun CardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    @StringRes errorRes: Int? = null,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        isError = errorRes != null,
        onValueChange = onValueChange,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        placeholder = { Text(text = placeholder, style = MaterialTheme.typography.bodyMedium) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTheme.colors.selected,
            unfocusedBorderColor = AppTheme.colors.divider,
            focusedContainerColor = AppTheme.colors.surface,
            unfocusedContainerColor = AppTheme.colors.surfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        supportingText = {
            errorRes?.let {
                Text(
                    text = stringResource(it),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
    )
}
