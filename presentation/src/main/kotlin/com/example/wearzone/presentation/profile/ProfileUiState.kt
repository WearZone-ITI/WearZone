package com.example.wearzone.presentation.profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.presentation.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Content(
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
        val recentOrders: ImmutableList<RecentOrderUiModel>,
        @param:StringRes val currencyRes: Int = R.string.profile_currency_usd,
        val isAuthenticated: Boolean,
    ) : ProfileUiState

    data class Error(@param:StringRes val messageRes: Int) : ProfileUiState
}

data class RecentOrderUiModel(
    val id: String,
    @param:StringRes val statusRes: Int,
    @param:StringRes val titleRes: Int,
    @param:DrawableRes val imageRes: Int,
    @param:StringRes val imageDescriptionRes: Int,
)

internal fun profileRecentOrders() = persistentListOf(
    RecentOrderUiModel(
        id = "order_noir_structura",
        statusRes = R.string.profile_recent_order_delivered,
        titleRes = R.string.profile_recent_order_bag,
        imageRes = R.drawable.onboarding_shopping_flow,
        imageDescriptionRes = R.string.profile_recent_order_bag_image_description,
    ),
    RecentOrderUiModel(
        id = "order_aura_hoops",
        statusRes = R.string.profile_recent_order_processing,
        titleRes = R.string.profile_recent_order_earrings,
        imageRes = R.drawable.onboarding_style_discovery,
        imageDescriptionRes = R.string.profile_recent_order_earrings_image_description,
    ),
)
