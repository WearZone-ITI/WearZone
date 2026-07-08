package com.example.wearzone.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun ProductTopBar(
    title: String,
    cartItemCount: Int,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(46.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surface)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = AppTheme.colors.textPrimary
            )
        }

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            maxLines = 1
        )

        CartIconButton(
            cartItemCount = cartItemCount,
            onClick = onCartClick,
            modifier = Modifier.align(Alignment.CenterEnd),
            iconButtonModifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surface),
        )
    }
}
