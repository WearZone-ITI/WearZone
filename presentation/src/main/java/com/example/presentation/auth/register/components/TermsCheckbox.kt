package com.example.presentation.auth.register.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.presentation.common.theme.MidnightSlate
import com.example.presentation.common.theme.OnSurfaceVariant

@Composable
fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val agreementPrefix = stringResource(R.string.terms_agreement_prefix)
    val terms = stringResource(R.string.terms_of_service)
    val and = stringResource(R.string.terms_and)
    val privacy = stringResource(R.string.privacy_policy)
    val period = stringResource(R.string.terms_period)

    val annotatedText = buildAnnotatedString {

        append(agreementPrefix)

        pushStringAnnotation(
            tag = TermsAnnotations.TERMS,
            annotation = TermsAnnotations.TERMS_VALUE,
        )

        withStyle(
            SpanStyle(
                color = MidnightSlate,
                textDecoration = TextDecoration.Underline,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            ),
        ) {
            append(" ")
            append(terms)
        }

        pop()
        append(" ")
        append(and)

        pushStringAnnotation(
            tag = TermsAnnotations.PRIVACY,
            annotation = TermsAnnotations.PRIVACY_VALUE,
        )

        withStyle(
            SpanStyle(
                color = MidnightSlate,
                textDecoration = TextDecoration.Underline,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            ),
        ) {
            append(" ")
            append(privacy)
        }

        pop()

        append(" ")
        append(period)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MidnightSlate,
                uncheckedColor = OnSurfaceVariant,
            ),
        )

        Spacer(Modifier.width(4.dp))

        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = OnSurfaceVariant,
            ),
        ) { offset ->

            annotatedText.getStringAnnotations(
                TermsAnnotations.TERMS,
                offset,
                offset,
            ).firstOrNull()?.let { onTermsClick() }

            annotatedText.getStringAnnotations(
                TermsAnnotations.PRIVACY, offset, offset
            ).firstOrNull()?.let { onPrivacyClick() }
        }
    }
}


private object TermsAnnotations {
    const val TERMS = "TERMS"
    const val TERMS_VALUE = "terms"

    const val PRIVACY = "PRIVACY"
    const val PRIVACY_VALUE = "privacy"
}