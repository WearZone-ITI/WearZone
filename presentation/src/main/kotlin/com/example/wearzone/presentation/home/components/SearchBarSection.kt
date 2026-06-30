package com.example.wearzone.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun SearchBarSection(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(colorResource(id = R.color.search_bar_background))
            .clickable(onClick = onClick)
            .background(AppTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Search,
                contentDescription = stringResource(id = R.string.content_desc_search),
                tint = Color.Gray,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.search_placeholder),
                color = Color.Gray,
                fontSize = 16.sp,
                tint = AppTheme.colors.textSecondary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.search_placeholder),
                color = AppTheme.colors.textSecondary,
                fontSize = 16.sp
            )
        }
    }
}
