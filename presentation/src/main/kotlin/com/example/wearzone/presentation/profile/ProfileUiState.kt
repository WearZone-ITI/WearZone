package com.example.wearzone.presentation.profile

import androidx.annotation.StringRes
import com.example.presentation.R
import kotlinx.collections.immutable.ImmutableList

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Guest(
        val cartItemCount: Int = 0,
    ) : ProfileUiState

    data class Content(
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
        val recentOrders: ImmutableList<RecentOrderUiModel>,
        @param:StringRes val currencyRes: Int = R.string.profile_currency_usd,
        val isAuthenticated: Boolean,
        val cartItemCount: Int = 0,
    ) : ProfileUiState

    data class Error(@param:StringRes val messageRes: Int) : ProfileUiState
}

data class RecentOrderUiModel(
    val id: String,
    @param:StringRes val statusRes: Int? = null,
    val statusLabel: String? = null,
    val title: String,
    val imageUrl: String?,
    @param:StringRes val imageDescriptionRes: Int? = null,
)
