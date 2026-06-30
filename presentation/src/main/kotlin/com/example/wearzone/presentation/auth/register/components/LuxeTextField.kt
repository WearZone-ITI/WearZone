package com.example.wearzone.presentation.auth.register.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun LuxeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> AppTheme.colors.error
            else -> AppTheme.colors.selected
        },
        animationSpec = tween(durationMillis = 200),
        label = "border_color",
    )
    val borderWidth = if (isFocused || isError) 1.5.dp else 0.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))

        BasicTextField(
            value               = value,
            onValueChange       = onValueChange,
            singleLine          = singleLine,
            keyboardOptions     = keyboardOptions,
            visualTransformation= visualTransformation,
            interactionSource   = interactionSource,
            textStyle           = MaterialTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
            cursorBrush         = SolidColor(AppTheme.colors.selected),
            decorationBox       = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surface, RoundedCornerShape(8.dp))
                        .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text  = placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppTheme.colors.textSecondary,
                                )
                            }
                            innerTextField()
                        }
                        trailingIcon?.invoke()
                    }
                }
            },
        )

        if (isError && errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text  = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.error,
            )
        }
    }
}
