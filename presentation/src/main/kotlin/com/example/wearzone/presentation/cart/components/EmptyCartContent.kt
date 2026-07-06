package com.example.wearzone.presentation.cart.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.presentation.R
import com.example.wearzone.presentation.common.PremiumEmptyState

@Composable
fun EmptyCartContent(
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumEmptyState(
        modifier = modifier,
        lottieResId = R.raw.cart_is_empty,
        title = stringResource(id = R.string.cart_empty_title),
        description = stringResource(id = R.string.cart_empty_subtitle),
        buttonText = stringResource(id = R.string.cart_shop_now),
        onButtonClick = onContinueShopping
    )
}
