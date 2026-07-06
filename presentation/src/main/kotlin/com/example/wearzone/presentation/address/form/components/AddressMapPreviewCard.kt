package com.example.wearzone.presentation.address.form.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditLocation
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.presentation.BuildConfig
import com.example.presentation.R
import com.example.wearzone.presentation.address.form.SelectedLocationUiModel
import com.example.wearzone.presentation.common.theme.AppTheme
import java.util.Locale

@Composable
fun AddressMapPreviewCard(
    selectedLocation: SelectedLocationUiModel,
    onChangeOnMap: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MapStaticImage(
                latitude = selectedLocation.latitude,
                longitude = selectedLocation.longitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = stringResource(R.string.address_selected_location),
                    tint = AppTheme.colors.selected,
                    modifier = Modifier.size(22.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedLocation.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (selectedLocation.subtitle.isNotBlank()) {
                        Text(
                            text = selectedLocation.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.address_clear_location),
                        tint = AppTheme.colors.textSecondary,
                    )
                }
            }
            TextButton(onClick = onChangeOnMap, modifier = Modifier.align(Alignment.End)) {
                Icon(
                    imageVector = Icons.Outlined.EditLocation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                Text(text = stringResource(R.string.address_change_on_map))
            }
        }
    }
}

@Composable
fun MapStaticImage(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
) {
    val imageUrl = rememberMapboxStaticUrl(latitude = latitude, longitude = longitude)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl == null) {
            Text(
                text = stringResource(R.string.address_map_preview_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.address_map_preview),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

fun rememberMapboxStaticUrl(latitude: Double, longitude: Double): String? {
    val token = BuildConfig.MAPBOX_ACCESS_TOKEN.normalizedPublicMapboxToken() ?: return null
    val lon = longitude.formatCoordinate()
    val lat = latitude.formatCoordinate()
    return "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/" +
            "pin-s+1A1A1B($lon,$lat)/$lon,$lat,15,0/720x360@2x?access_token=$token"
}

private fun Double.formatCoordinate(): String =
    String.format(Locale.US, "%.6f", this)

private fun String.normalizedPublicMapboxToken(): String? {
    val token = trim().removeSurrounding("\"")
    return token.takeIf { it.startsWith(PUBLIC_TOKEN_PREFIX) }
}

private const val PUBLIC_TOKEN_PREFIX = "pk."
