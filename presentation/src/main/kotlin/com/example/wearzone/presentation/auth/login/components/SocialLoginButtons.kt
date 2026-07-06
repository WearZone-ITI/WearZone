package com.example.wearzone.presentation.auth.login.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.auth.login.LoginUiIntent
import com.example.wearzone.presentation.common.theme.AppTheme

@Composable
fun SocialLoginButtons(
    onIntent: (LoginUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = { onIntent(LoginUiIntent.OnGoogleSignInClicked) },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, AppTheme.colors.border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.textPrimary)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = stringResource(R.string.content_desc_google_sign_in),
                tint = Color.Unspecified
            )
        }

        OutlinedButton(
            onClick = { onIntent(LoginUiIntent.OnGuestModeClicked) },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, AppTheme.colors.border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.textPrimary)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_guest),
                contentDescription = "Guest Mode",
                tint = Color.Unspecified
            )
        }
    }
}
