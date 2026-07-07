package com.example.wearzone.presentation.checkout.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.R
import com.example.wearzone.presentation.checkout.CheckoutPaymentMethodUi
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun PaymentMethodSelector(
    selectedMethod: CheckoutPaymentMethodUi,
    onMethodSelected: (CheckoutPaymentMethodUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.checkout_payment_method_title),
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        PaymentOptionItem(
            title = stringResource(R.string.checkout_payment_cash_on_delivery),
            description = stringResource(R.string.checkout_payment_cash_on_delivery_description),
            isSelected = selectedMethod == CheckoutPaymentMethodUi.CashOnDelivery,
            onClick = { onMethodSelected(CheckoutPaymentMethodUi.CashOnDelivery) }
        )

        PaymentOptionItem(
            title = stringResource(R.string.checkout_payment_credit_card),
            description = stringResource(R.string.checkout_payment_credit_card_description),
            isSelected = selectedMethod == CheckoutPaymentMethodUi.CreditCard,
            onClick = { onMethodSelected(CheckoutPaymentMethodUi.CreditCard) },
            showLogos = true
        )
    }
}

@Composable
private fun PaymentOptionItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    showLogos: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) AppTheme.colors.selected else AppTheme.colors.divider
        ),
        color = if (isSelected) AppTheme.colors.selected.copy(alpha = 0.05f) else AppTheme.colors.card
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (isSelected) AppTheme.colors.selected else AppTheme.colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.textSecondary
                )
                
                if (showLogos) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BrandBadge("VISA")
                        BrandBadge("Mastercard")
                        BrandBadge("Meeza")
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandBadge(name: String) {
    Surface(
        color = AppTheme.colors.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
