package com.example.presentation.auth.login.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.presentation.auth.login.LoginUiIntent
import com.example.presentation.common.theme.AppColors
import com.example.presentation.common.theme.AppTypography

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
            border = BorderStroke(1.dp, AppColors.Primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextPrimary)
        ) {
            Icon(
                painter = painterResource(id = com.example.presentation.R.drawable.ic_google),
                contentDescription = "Sign in with Google",
                tint = Color.Unspecified
            )
        }

        OutlinedButton(
            onClick = { onIntent(LoginUiIntent.OnAppleSignInClicked) },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, AppColors.Primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextPrimary)
        ) {
            Icon(
                painter = painterResource(id = com.example.presentation.R.drawable.ic_apple),
                contentDescription = "Sign in with Apple",
                tint = Color.Unspecified
            )
        }
    }
}
