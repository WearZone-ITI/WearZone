package com.example.wearzone.presentation.auth.register.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.common.theme.StrengthFair
import com.example.wearzone.presentation.common.theme.StrengthGood
import com.example.wearzone.presentation.common.theme.StrengthStrong
import com.example.wearzone.presentation.common.theme.StrengthWeak

@Composable
fun PasswordStrengthBar(
    password: String,
    modifier: Modifier = Modifier,
) {
    val strength = evaluatePasswordStrength(password)
    val inactiveSegmentColor = AppTheme.colors.surfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(4) { index ->
                val targetColor = segmentColor(
                    segmentIndex = index,
                    activeSegments = strength.segments,
                    strength = strength,
                    inactiveColor = inactiveSegmentColor,
                )
                val animatedColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(300),
                    label = "segment_$index",
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(animatedColor, RoundedCornerShape(2.dp)),
                )
            }
        }
        if (strength != PasswordStrength.Empty) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.password_strength,
                    stringResource(strength.label)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = segmentColor(
                    segmentIndex = 0,
                    activeSegments = strength.segments,
                    strength = strength,
                    inactiveColor = inactiveSegmentColor,
                ),
            )
        }
    }
}

enum class PasswordStrength(val label: Int, val segments: Int) {
    Empty(R.string.empty, 0),
    Weak(R.string.password_strength_weak, 1),
    Fair(R.string.password_strength_fair, 2),
    Good(R.string.password_strength_good, 3),
    Strong(R.string.password_strength_strong, 4),
}

private fun evaluatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.Empty
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return when (score) {
        0, 1 -> PasswordStrength.Weak
        2 -> PasswordStrength.Fair
        3 -> PasswordStrength.Good
        else -> PasswordStrength.Strong
    }
}

private fun segmentColor(
    segmentIndex: Int,
    activeSegments: Int,
    strength: PasswordStrength,
    inactiveColor: Color,
): Color {
    if (segmentIndex >= activeSegments) return inactiveColor
    return when (strength) {
        PasswordStrength.Weak -> StrengthWeak
        PasswordStrength.Fair -> StrengthFair
        PasswordStrength.Good -> StrengthGood
        PasswordStrength.Strong -> StrengthStrong
        PasswordStrength.Empty -> inactiveColor
    }
}
