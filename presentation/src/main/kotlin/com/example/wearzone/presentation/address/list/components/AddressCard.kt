package com.example.wearzone.presentation.address.list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.address.list.CustomerAddressUiModel
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun AddressCard(
    address: CustomerAddressUiModel,
    isActionEnabled: Boolean,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onSetDefaultClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (address.isDefault) AppTheme.colors.selected else AppTheme.colors.divider

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppTheme.colors.surface,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (address.isDefault) 4.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 180.dp)
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.address_card_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (address.isDefault) {
                    DefaultBadge()
                }
                IconButton(
                    onClick = onEditClicked,
                    enabled = isActionEnabled,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.address_edit),
                        tint = AppTheme.colors.textSecondary,
                    )
                }
                IconButton(
                    onClick = onDeleteClicked,
                    enabled = isActionEnabled,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.address_delete),
                        tint = AppTheme.colors.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = address.recipientName,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = address.phone,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = address.addressLine,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = address.cityLine,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = address.countryLine,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (!address.isDefault) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onSetDefaultClicked,
                    enabled = isActionEnabled,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(R.string.address_set_default),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.address_set_default))
                }
            }
        }
    }
}

@Composable
private fun DefaultBadge() {
    Surface(
        color = AppTheme.colors.selected,
        contentColor = AppTheme.colors.onAccent,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = stringResource(R.string.address_default_badge),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
