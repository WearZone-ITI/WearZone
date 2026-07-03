package com.example.wearzone.presentation.checkout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun PromoCodeCard(
    promoCodeText: String,
    appliedDiscountCode: String?,
    discountErrorRes: Int?,
    isApplyingDiscount: Boolean,
    onPromoCodeChanged: (String) -> Unit,
    onApplyClicked: () -> Unit,
    onRemoveClicked: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.checkout_promo_code_title),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (appliedDiscountCode != null) {
                AppliedDiscountRow(
                    code = appliedDiscountCode,
                    onRemoveClicked = onRemoveClicked,
                )
            } else {
                PromoCodeInputRow(
                    promoCodeText = promoCodeText,
                    discountErrorRes = discountErrorRes,
                    isApplyingDiscount = isApplyingDiscount,
                    onPromoCodeChanged = onPromoCodeChanged,
                    onApplyClicked = onApplyClicked,
                )
            }
        }
    }
}

@Composable
private fun AppliedDiscountRow(
    code: String,
    onRemoveClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onRemoveClicked,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(
                text = stringResource(R.string.checkout_remove_discount),
                color = AppTheme.colors.selected,
            )
        }
    }
}

@Composable
private fun PromoCodeInputRow(
    promoCodeText: String,
    discountErrorRes: Int?,
    isApplyingDiscount: Boolean,
    onPromoCodeChanged: (String) -> Unit,
    onApplyClicked: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = promoCodeText,
                onValueChange = onPromoCodeChanged,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(R.string.checkout_promo_code_placeholder),
                        color = AppTheme.colors.textSecondary,
                    )
                },
                singleLine = true,
                isError = discountErrorRes != null,
                enabled = !isApplyingDiscount,
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onApplyClicked() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppTheme.colors.surfaceVariant,
                    unfocusedContainerColor = AppTheme.colors.surfaceVariant,
                    disabledContainerColor = AppTheme.colors.surfaceVariant,
                    focusedBorderColor = AppTheme.colors.selected,
                    unfocusedBorderColor = AppTheme.colors.surfaceVariant,
                    errorBorderColor = AppTheme.colors.error,
                ),
            )
            Button(
                onClick = onApplyClicked,
                enabled = !isApplyingDiscount,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.surfaceVariant,
                    contentColor = AppTheme.colors.textPrimary,
                    disabledContainerColor = AppTheme.colors.surfaceVariant,
                    disabledContentColor = AppTheme.colors.textSecondary,
                ),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                if (isApplyingDiscount) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.textPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.heightIn(min = 20.dp),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.checkout_apply_discount),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        if (discountErrorRes != null) {
            Text(
                text = stringResource(discountErrorRes),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.error,
            )
        }
    }
}
