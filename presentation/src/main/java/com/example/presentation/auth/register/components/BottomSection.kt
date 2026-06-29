package com.example.presentation.auth.register.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.presentation.R
import com.example.presentation.common.theme.MidnightSlate
import com.example.presentation.common.theme.OnSurfaceVariant

@Composable
fun BottomSection(
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(
                Alignment.CenterHorizontally,
            ),
    ) {
        Text(
            text = stringResource(
                R.string.already_have_account,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
        )
        val loginText = buildAnnotatedString {

            pushStringAnnotation(
                "LOGIN",
                "login",
            )

            withStyle(
                SpanStyle(
                    color = MidnightSlate,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(
                    stringResource(
                        R.string.log_in,
                    ),
                )
            }

            pop()
        }
        ClickableText(
            text = loginText,
            style = MaterialTheme.typography.bodyMedium,
            onClick = { offset ->
                loginText.getStringAnnotations(
                    "LOGIN",
                    offset,
                    offset,
                ).firstOrNull()?.let {
                    onLoginClicked()
                }
            },
        )
    }
}