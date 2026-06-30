package com.example.wearzone.presentation.profile.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    @StringRes titleRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes trailingRes: Int? = null,
    isDestructive: Boolean = false,
) {
    val contentColor =
        if (isDestructive) AppTheme.colors.error else AppTheme.colors.textPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(titleRes),
            tint = if (isDestructive) AppTheme.colors.error else AppTheme.colors.textSecondary,
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailingRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!isDestructive) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = stringResource(titleRes),
                tint = AppTheme.colors.textSecondary,
            )
        }
    }
}
