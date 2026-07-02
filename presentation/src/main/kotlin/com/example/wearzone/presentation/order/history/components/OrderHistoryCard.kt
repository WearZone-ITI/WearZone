package com.example.wearzone.presentation.order.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.order.history.OrderThumbnailUiModel
import com.example.wearzone.presentation.order.history.OrderHistoryUiModel
import com.example.wearzone.presentation.order.history.OrderStatusTone
import com.example.wearzone.presentation.order.history.OrderStatusUiModel

@Composable
fun OrderHistoryCard(
    order: OrderHistoryUiModel,
    onViewDetailsClick: () -> Unit,
    onTrackPackageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val orderName = order.displayName.ifBlank {
        stringResource(R.string.order_history_unknown_order)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AppTheme.colors.surface,
        border = BorderStroke(1.dp, AppTheme.colors.divider.copy(alpha = 0.7f)),
        shadowElevation = 7.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.order_history_order_number, orderName),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTheme.colors.textSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = order.placedDate.toPlacedDateText(),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 7.dp),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = order.formattedTotalPrice,
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    order.statuses.firstOrNull()?.let { status ->
                        Spacer(modifier = Modifier.height(7.dp))
                        OrderStatusBadge(status = status)
                    }
                }
            }

            if (order.thumbnails.isNotEmpty()) {
                HorizontalDivider(
                    color = AppTheme.colors.divider,
                    modifier = Modifier.padding(top = 22.dp, bottom = 20.dp),
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(order.thumbnails, key = { it.id }) { thumbnail ->
                        OrderThumbnail(
                            thumbnail = thumbnail,
                            orderName = orderName,
                        )
                    }
                }
            }

            HorizontalDivider(
                color = AppTheme.colors.divider,
                modifier = Modifier.padding(top = 24.dp, bottom = 18.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!order.canTrack) {
                    Spacer(modifier = Modifier.weight(1f))
                }
                OutlinedButton(
                    onClick = onViewDetailsClick,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, AppTheme.colors.textPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppTheme.colors.textPrimary,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.order_history_view_details),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (order.canTrack) {
                    Button(
                        onClick = onTrackPackageClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.textPrimary,
                            contentColor = AppTheme.colors.surface,
                        ),
                        modifier = Modifier
                            .weight(1.08f)
                            .height(48.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.order_history_track_package),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderThumbnail(
    thumbnail: OrderThumbnailUiModel,
    orderName: String,
) {
    Box(
        modifier = Modifier
            .size(width = 104.dp, height = 128.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (thumbnail.imageUrl.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.order_history_image_placeholder),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            AsyncImage(
                model = thumbnail.imageUrl,
                contentDescription = stringResource(
                    R.string.order_history_product_image_content_description,
                    orderName,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
            )
        }
    }
}

@Composable
private fun OrderStatusBadge(
    status: OrderStatusUiModel,
) {
    val colors = AppTheme.colors
    val backgroundColor = when (status.tone) {
        OrderStatusTone.Success -> colors.success.copy(alpha = 0.14f)
        OrderStatusTone.Warning -> colors.warning.copy(alpha = 0.16f)
        OrderStatusTone.Error -> colors.error.copy(alpha = 0.14f)
        OrderStatusTone.Neutral -> colors.surfaceVariant
    }
    val contentColor = when (status.tone) {
        OrderStatusTone.Success -> colors.success
        OrderStatusTone.Warning -> colors.warning
        OrderStatusTone.Error -> colors.error
        OrderStatusTone.Neutral -> colors.textSecondary
    }
    val icon = when (status.tone) {
        OrderStatusTone.Success -> Icons.Outlined.CheckCircle
        OrderStatusTone.Warning -> Icons.Outlined.Schedule
        OrderStatusTone.Error -> Icons.Outlined.WarningAmber
        OrderStatusTone.Neutral -> Icons.Outlined.Info
    }
    val label = status.labelRes?.let { stringResource(it) } ?: status.label.orEmpty()

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun String.toPlacedDateText(): String =
    if (isBlank()) {
        stringResource(R.string.order_history_placed_date_unavailable)
    } else {
        stringResource(R.string.order_history_placed_on, this)
    }
