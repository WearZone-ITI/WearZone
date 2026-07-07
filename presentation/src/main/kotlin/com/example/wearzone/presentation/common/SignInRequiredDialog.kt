package com.example.wearzone.presentation.common

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.theme.WearZoneTheme

@Composable
fun SignInRequiredDialog(
    @StringRes messageRes: Int = R.string.sign_in_required_message,
    onSignInRegister: () -> Unit,
    onContinueBrowsing: () -> Unit,
) {
    Dialog(
        onDismissRequest = onContinueBrowsing,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val isCompactHeight = maxHeight < 420.dp
            val headerHeight = if (isCompactHeight) 96.dp else 136.dp
            val badgeSize = if (isCompactHeight) 68.dp else 84.dp
            val iconSize = if (isCompactHeight) 28.dp else 34.dp
            val badgeBorderWidth = if (isCompactHeight) 4.dp else 5.dp
            val contentTopPadding = if (isCompactHeight) 20.dp else 28.dp
            val contentBottomPadding = if (isCompactHeight) 22.dp else 30.dp
            val titleSpacerHeight = if (isCompactHeight) 10.dp else 14.dp
            val buttonSpacerHeight = if (isCompactHeight) 20.dp else 30.dp

            Surface(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight),
                shape = RoundedCornerShape(30.dp),
                color = AppTheme.colors.surface,
                tonalElevation = 0.dp,
                shadowElevation = 14.dp,
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        AppTheme.colors.accent.copy(alpha = 0.42f),
                                        AppTheme.colors.surface,
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(badgeSize)
                                .clip(CircleShape)
                                .background(AppTheme.colors.surfaceVariant)
                                .border(
                                    width = badgeBorderWidth,
                                    color = AppTheme.colors.surface,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = stringResource(R.string.sign_in_required_lock_content_description),
                                tint = AppTheme.colors.textPrimary,
                                modifier = Modifier.size(iconSize),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 28.dp,
                                top = contentTopPadding,
                                end = 28.dp,
                                bottom = contentBottomPadding,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_in_required_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(titleSpacerHeight))
                        Text(
                            text = stringResource(id = messageRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(buttonSpacerHeight))
                        Button(
                            onClick = onSignInRegister,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.textPrimary,
                                contentColor = AppTheme.colors.surface,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.sign_in_register),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onContinueBrowsing,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.4.dp, AppTheme.colors.textPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppTheme.colors.textPrimary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.continue_browsing),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview(){
    WearZoneTheme {
        SignInRequiredDialog(
            onSignInRegister = { },
            onContinueBrowsing = { },
        )
    }
}