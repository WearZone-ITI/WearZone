package com.example.wearzone.presentation.wishlist.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.presentation.R
import com.example.wearzone.presentation.common.PremiumEmptyState

@Composable
fun WishlistEmptyState(
    onContinueBrowsing: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumEmptyState(
        modifier = modifier,
        lottieResId = R.raw.no_favourites_found,
        title = stringResource(id = R.string.wishlist_empty_title),
        description = stringResource(id = R.string.wishlist_empty_subtitle),
        buttonText = stringResource(id = R.string.wishlist_discover_styles),
        onButtonClick = onContinueBrowsing
    )
}
