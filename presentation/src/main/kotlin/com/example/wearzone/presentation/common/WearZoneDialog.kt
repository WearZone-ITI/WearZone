package com.example.wearzone.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun WearZoneDialog(
    title: String,
    message: String? = null,
    confirmText: String? = null,
    cancelText: String? = null,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    tone: WearZoneDialogTone = WearZoneDialogTone.Default,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val toneColor = when (tone) {
        WearZoneDialogTone.Default -> colors.selected
        WearZoneDialogTone.Info -> colors.accent
        WearZoneDialogTone.Success -> colors.success
        WearZoneDialogTone.Warning -> colors.warning
        WearZoneDialogTone.Destructive -> colors.error
    }
    val confirmContainerColor = when (tone) {
        WearZoneDialogTone.Warning -> {
            if (isSystemInDarkTheme()) {
                colors.warning
            } else {
                Color.Black
            }
        }
        WearZoneDialogTone.Destructive -> colors.error
        WearZoneDialogTone.Success -> colors.success
        else -> colors.selected
    }
    val confirmContentColor = when (tone) {
        WearZoneDialogTone.Warning -> {
            if (isSystemInDarkTheme()) {
                colors.onAccent
            } else {
                Color.White
            }
        }
        else -> colors.onAccent
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
                shape = RoundedCornerShape(30.dp),
                color = colors.surface,
                tonalElevation = 0.dp,
                shadowElevation = 18.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(toneColor.copy(alpha = 0.14f))
                                .border(
                                    width = 1.dp,
                                    color = toneColor.copy(alpha = 0.24f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = iconContentDescription,
                                tint = toneColor,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(22.dp))
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    if (!message.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }

                    if (content != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            content()
                        }
                    }

                    if ((confirmText != null && onConfirm != null) || cancelText != null) {
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    if (confirmText != null && onConfirm != null) {
                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmContainerColor,
                                contentColor = confirmContentColor,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        ) {
                            Text(
                                text = confirmText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    if (cancelText != null) {
                        if (confirmText != null && onConfirm != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        OutlinedButton(
                            onClick = onCancel ?: onDismiss,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.4.dp, colors.selected),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.selected,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        ) {
                            Text(
                                text = cancelText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class WearZoneDialogTone {
    Default,
    Info,
    Success,
    Warning,
    Destructive,
}
