package com.example.wearzone.presentation.common

import androidx.benchmark.traceprocessor.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.R
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.textPrimary,
            maxLines = 1
        )

        BadgedBox(
            modifier = Modifier.align(Alignment.CenterEnd),
            badge = {
                if (cartItemCount > 0) {
                    Badge {
                        Text(cartItemCount.toString())
                    }
                }
            }
        ) {
            IconButton(
                onClick = onCartClick,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surface)
            ) {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = AppTheme.colors.textPrimary
                )
            }
        }
    }
}