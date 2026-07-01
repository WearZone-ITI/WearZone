package com.example.wearzone.presentation.wishlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.wearzone.presentation.common.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveFavoriteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.background)
            .border(1.dp, AppTheme.colors.surfaceVariant, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Destructive Icon Indicator
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.error.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove",
                    tint = AppTheme.colors.error,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Remove Item?",
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "This item will be removed from your wishlist.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Primary CTA (Destructive)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppTheme.colors.error)
                    .clickable { onConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Remove",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.onAccent
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Secondary Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5f.dp, AppTheme.colors.selected, RoundedCornerShape(18.dp))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Keep in Wishlist",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.selected
                )
            }
        }
    }
}
