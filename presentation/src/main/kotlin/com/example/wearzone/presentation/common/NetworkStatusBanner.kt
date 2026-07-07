package com.example.wearzone.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R

/**
 * A professional, user-friendly status banner for displaying network errors or other information.
 *
 * @param message The message to display. If it's a raw exception string, it will be mapped to a user-friendly version.
 * @param modifier Modifier for the root layout.
 */
@Composable
fun NetworkStatusBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    val noInternetMessage = stringResource(R.string.no_internet_description)
    
    val userFriendlyMessage = remember(message, noInternetMessage) {
        message.toUserFriendlyMessage(noInternetMessage)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = userFriendlyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

private fun String.toUserFriendlyMessage(noInternetMessage: String): String {
    return if (this.contains("network", ignoreCase = true) ||
        this.contains("timeout", ignoreCase = true) ||
        this.contains("interrupted", ignoreCase = true) ||
        this.contains("unreachable", ignoreCase = true) ||
        this.contains("host", ignoreCase = true)
    ) {
        noInternetMessage
    } else {
        this
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun NetworkStatusBannerPreview() {
    com.example.wearzone.presentation.common.theme.WearZoneTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            NetworkStatusBanner(message = "Please check your internet connection and try again.")
            Spacer(modifier = Modifier.height(16.dp))
            NetworkStatusBanner(message = "A network error (such as timeout, interrupted connection or unreachable host) has occurred.")
            Spacer(modifier = Modifier.height(16.dp))
            NetworkStatusBanner(message = "Something went wrong.")
        }
    }
}
