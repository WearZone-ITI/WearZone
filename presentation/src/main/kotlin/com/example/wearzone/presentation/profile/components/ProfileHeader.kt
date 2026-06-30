package com.example.wearzone.presentation.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun ProfileHeader(
    displayName: String?,
    email: String?,
    modifier: Modifier = Modifier,
) {
    val resolvedName = displayName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.profile_guest_name)
    val resolvedEmail = email?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.profile_guest_email)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.profile_avatar_static),
            contentDescription = stringResource(R.string.profile_static_avatar_content_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.divider),
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = stringResource(R.string.profile_greeting, resolvedName),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = resolvedEmail,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}
