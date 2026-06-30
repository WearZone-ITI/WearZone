package com.example.wearzone.presentation.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wearzone.presentation.profile.RecentOrderUiModel
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun RecentOrderCard(
    order: RecentOrderUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = AppTheme.colors.card,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(order.imageRes),
                contentDescription = stringResource(order.imageDescriptionRes),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppTheme.colors.divider),
            )
            Text(
                text = stringResource(order.statusRes),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.textSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = stringResource(order.titleRes),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
