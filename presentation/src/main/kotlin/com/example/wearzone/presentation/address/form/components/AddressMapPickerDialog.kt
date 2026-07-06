package com.example.wearzone.presentation.address.form.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.presentation.BuildConfig as PresentationBuildConfig
import com.example.presentation.R
import com.example.wearzone.presentation.address.form.AddressSuggestionUiModel
import com.example.wearzone.presentation.address.form.SelectedLocationUiModel
import com.example.wearzone.presentation.common.theme.AppTheme
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AddressMapPickerDialog(
    latitude: Double,
    longitude: Double,
    currentLatitude: Double?,
    currentLongitude: Double?,
    isResolving: Boolean,
    selectedLocation: SelectedLocationUiModel?,
    searchQuery: String,
    suggestions: ImmutableList<AddressSuggestionUiModel>,
    isSearching: Boolean,
    messageRes: Int?,
    isResolvingCurrentLocation: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onSearchCleared: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onCoordinatesChanged: (Double, Double) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppTheme.colors.background,
        ) {
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(longitude, latitude))
                    zoom(DEFAULT_ZOOM)
                    pitch(0.0)
                    bearing(0.0)
                }
            }

            fun flyTo(point: AddressMapPoint, zoom: Double = DEFAULT_ZOOM) {
                mapViewportState.flyTo(
                    cameraOptions = cameraOptions {
                        center(point.toMapboxPoint())
                        zoom(zoom)
                        pitch(0.0)
                        bearing(0.0)
                    },
                    animationOptions = MapAnimationOptions.mapAnimationOptions {
                        duration(CAMERA_FLY_TO_DURATION_MILLIS)
                    },
                )
            }

            val selectedPoint = selectedLocation?.let {
                AddressMapPoint(latitude = latitude, longitude = longitude)
            }
            val currentPoint = remember(currentLatitude, currentLongitude) {
                if (currentLatitude != null && currentLongitude != null) {
                    AddressMapPoint(latitude = currentLatitude, longitude = currentLongitude)
                } else {
                    null
                }
            }

            LaunchedEffect(Unit) {
                if (selectedLocation == null) {
                    onUseCurrentLocation()
                }
            }

            LaunchedEffect(latitude, longitude) {
                flyTo(AddressMapPoint(latitude = latitude, longitude = longitude))
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenHeight = maxHeight
                val appBarHeight = screenHeight * 0.15f

                AddressMapBoxContent(
                    mapViewportState = mapViewportState,
                    currentPoint = currentPoint,
                    selectedPoint = selectedPoint,
                    onPointSelected = { point ->
                        onCoordinatesChanged(point.latitude, point.longitude)
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .safeContentPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(appBarHeight * 0.4f),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    AddressMapSearchField(
                        query = searchQuery,
                        isSearching = isSearching,
                        suggestions = suggestions,
                        messageRes = messageRes,
                        onQueryChange = onSearchQueryChanged,
                        onClearClick = onSearchCleared,
                        onSuggestionClick = onSuggestionSelected,
                        modifier = Modifier.weight(1f),
                    )
                }

                FloatingActionButton(
                    onClick = onUseCurrentLocation,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 196.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                ) {
                    if (isResolvingCurrentLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = stringResource(R.string.address_use_current_location),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                AnimatedVisibility(
                    visible = selectedLocation != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        selectedLocation?.let { location ->
                            SelectedMapLocationCard(
                                location = location,
                                isResolving = isResolving,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            enabled = selectedLocation != null && !isResolving,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            if (isResolving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.address_use_this_location),
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }

                if (isResolvingCurrentLocation) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = false, onClick = {})
                            .background(Color.Black.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
private fun AddressMapBoxContent(
    mapViewportState: MapViewportState,
    currentPoint: AddressMapPoint?,
    selectedPoint: AddressMapPoint?,
    onPointSelected: (AddressMapPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accessToken = remember(PresentationBuildConfig.MAPBOX_ACCESS_TOKEN) {
        PresentationBuildConfig.MAPBOX_ACCESS_TOKEN.normalizedPublicMapboxToken()
    }
    if (accessToken == null) {
        MapUnavailableContent(modifier = modifier)
        return
    }

    MapboxOptions.accessToken = accessToken

    val selectedColor = MaterialTheme.colorScheme.primary
    val currentColor = Color(0xFF2196F3)
    val selectedMarker = rememberIconImage(
        key = R.drawable.ic_location_point,
        painter = painterResource(R.drawable.ic_location_point),
    )

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        style = { MapStyle(style = MAPBOX_STREETS_STYLE_URI) },
        scaleBar = {},
        compass = {},
        logo = {},
        attribution = {},
        onMapClickListener = { point ->
            onPointSelected(
                AddressMapPoint(
                    latitude = point.latitude(),
                    longitude = point.longitude(),
                ),
            )
            true
        },
    ) {
        MapEffect(MAPBOX_STREETS_STYLE_URI) { mapView ->
            mapView.mapboxMap.loadStyleUri(MAPBOX_STREETS_STYLE_URI)
        }

        currentPoint?.let { point ->
            CircleAnnotation(point = point.toMapboxPoint()) {
                circleRadius = 9.0
                circleColor = currentColor
                circleStrokeWidth = 3.0
                circleStrokeColor = Color.White
                circleOpacity = 0.9
            }
        }

        selectedPoint?.let { point ->
            PointAnnotation(point = point.toMapboxPoint()) {
                iconImage = selectedMarker
                iconAnchor = IconAnchor.BOTTOM
                iconSize = SELECTED_MARKER_ICON_SIZE
                iconColor = selectedColor
            }
        }
    }
}

@Composable
private fun MapUnavailableContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.address_interactive_map_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
private fun SelectedMapLocationCard(
    location: SelectedLocationUiModel,
    isResolving: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = location.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val subtitle = location.subtitle.ifBlank {
                    "${location.latitude.formatMapCoordinate()}, ${location.longitude.formatMapCoordinate()}"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (isResolving) {
                Spacer(modifier = Modifier.width(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Composable
private fun AddressMapSearchField(
    query: String,
    isSearching: Boolean,
    suggestions: ImmutableList<AddressSuggestionUiModel>,
    messageRes: Int?,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary),
            placeholder = {
                Text(
                    text = stringResource(R.string.address_search_hint),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.address_search_clear),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )

        AnimatedVisibility(
            visible = suggestions.isNotEmpty() || messageRes != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                if (suggestions.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                        itemsIndexed(
                            items = suggestions,
                            key = { _, suggestion -> suggestion.id },
                        ) { index, item ->
                            AddressMapSuggestionItem(
                                suggestion = item,
                                onClick = { onSuggestionClick(item.id) },
                            )
                            if (index < suggestions.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                } else if (messageRes != null) {
                    Text(
                        text = stringResource(messageRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressMapSuggestionItem(
    suggestion: AddressSuggestionUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            suggestion.subtitle.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class AddressMapPoint(
    val latitude: Double,
    val longitude: Double,
)

private fun AddressMapPoint.toMapboxPoint(): Point = Point.fromLngLat(longitude, latitude)

private fun String.normalizedPublicMapboxToken(): String? {
    val token = trim().removeSurrounding("\"")
    return token.takeIf { it.startsWith(PUBLIC_TOKEN_PREFIX) }
}

private fun Double.formatMapCoordinate(): String = String.format(Locale.US, "%.5f", this)

private const val PUBLIC_TOKEN_PREFIX = "pk."
private val MAPBOX_STREETS_STYLE_URI = Style.MAPBOX_STREETS
private const val DEFAULT_ZOOM = 15.0
private const val CAMERA_FLY_TO_DURATION_MILLIS = 900L
private const val SELECTED_MARKER_ICON_SIZE = 1.35
