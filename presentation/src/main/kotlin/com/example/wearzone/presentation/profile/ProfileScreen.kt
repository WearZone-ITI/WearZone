package com.example.wearzone.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.common.SignInRequiredDialog
import com.example.wearzone.presentation.common.TopBar
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.profile.components.LogoutConfirmationDialog
import com.example.wearzone.presentation.profile.components.ProfileHeader
import com.example.wearzone.presentation.profile.components.ProfileMenuRow
import com.example.wearzone.presentation.profile.components.RecentOrderCard
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSavedAddresses: () -> Unit,
    onNavigateToCart: () -> Unit,
    onContinueBrowsing: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSignInRequiredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                ProfileUiEffect.NavigateToLogin -> onNavigateToLogin()
                ProfileUiEffect.NavigateToRegister -> onNavigateToRegister()
                ProfileUiEffect.NavigateToSettings -> onNavigateToSettings()
                ProfileUiEffect.NavigateToWishlist -> onNavigateToWishlist()
                ProfileUiEffect.NavigateToOrders -> onNavigateToOrders()
                ProfileUiEffect.NavigateToCart -> onNavigateToCart()
                ProfileUiEffect.NavigateToSavedAddresses -> onNavigateToSavedAddresses()
                ProfileUiEffect.ShowLogoutConfirmation -> showLogoutDialog = true
                ProfileUiEffect.ShowSignInRequired -> showSignInRequiredDialog = true
                is ProfileUiEffect.ShowError -> coroutineScope.launch {
                    showCustomSnackbar(
                        context = context,
                        resId = effect.messageRes,
                        snackbarHostState = snackbarHostState
                    )                }
            }
        }
    }

    Scaffold(
        topBar = {
            val cartCount = when (val state = uiState) {
                is ProfileUiState.Content -> state.cartItemCount
                is ProfileUiState.Guest -> state.cartItemCount
                else -> 0
            }
            TopBar(cartItemCount = cartCount, onAddToCartClick = { viewModel.handleIntent(ProfileUiIntent.OnCardClicked) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        ProfileContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent,
            onContinueBrowsing = onContinueBrowsing,
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.handleIntent(ProfileUiIntent.OnLogoutConfirmed)
            },
            onDismiss = {
                showLogoutDialog = false
                viewModel.handleIntent(ProfileUiIntent.OnLogoutCancelled)
            },
        )
    }

    if (showSignInRequiredDialog) {
        SignInRequiredDialog(
            onSignIn = {
                showSignInRequiredDialog = false
                onNavigateToLogin()
            },
            onCreateAccount = {
                showSignInRequiredDialog = false
                onNavigateToRegister()
            },
            onContinueBrowsing = { showSignInRequiredDialog = false },
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onIntent: (ProfileUiIntent) -> Unit,
    onContinueBrowsing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        when (uiState) {
            ProfileUiState.Loading -> CircularProgressIndicator(
                color = AppTheme.colors.selected,
                modifier = Modifier.align(Alignment.Center),
            )

            is ProfileUiState.Error -> Text(
                text = stringResource(uiState.messageRes),
                color = AppTheme.colors.error,
                modifier = Modifier.align(Alignment.Center),
            )

            is ProfileUiState.Content -> ProfileLoadedContent(
                uiState = uiState,
                onIntent = onIntent,
            )

            is ProfileUiState.Guest -> ProfileGuestContent(
                onIntent = onIntent,
                onContinueBrowsing = onContinueBrowsing,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun ProfileGuestContent(
    onIntent: (ProfileUiIntent) -> Unit,
    onContinueBrowsing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.profile_guest_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.profile_guest_required_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Button(
            onClick = { onIntent(ProfileUiIntent.OnSignInClicked) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.sign_in))
        }
        TextButton(
            onClick = { onIntent(ProfileUiIntent.OnCreateAccountClicked) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.create_account))
        }
        TextButton(
            onClick = onContinueBrowsing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.continue_browsing))
        }
    }
}

@Composable
private fun ProfileLoadedContent(
    uiState: ProfileUiState.Content,
    onIntent: (ProfileUiIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    ) {
        item {
            ProfileHeader(
                displayName = uiState.displayName,
                email = uiState.email,
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item { HorizontalDivider(color = AppTheme.colors.divider) }
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item {
            RecentOrdersHeader(
                onViewAllClicked = { onIntent(ProfileUiIntent.OnMyOrdersClicked) },
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                uiState.recentOrders.forEach { order ->
                    RecentOrderCard(
                        order = order,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item { HorizontalDivider(color = AppTheme.colors.divider) }
        items(profileRows(uiState.currencyRes), key = { it.titleRes }) { row ->
            ProfileMenuRow(
                icon = row.icon,
                titleRes = row.titleRes,
                trailingRes = row.trailingRes,
                isDestructive = row.isDestructive,
                onClick = { onIntent(row.intent) },
            )
            if (!row.isDestructive) {
                HorizontalDivider(color = AppTheme.colors.divider)
            }
        }
    }
}

@Composable
private fun RecentOrdersHeader(
    onViewAllClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.profile_recent_orders),
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.profile_view_all),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        IconButton(onClick = onViewAllClicked) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = stringResource(R.string.profile_view_all),
                tint = AppTheme.colors.textPrimary,
            )
        }
    }
}

private suspend fun showCustomSnackbar(
    context: android.content.Context,
    resId: Int,
    snackbarHostState: SnackbarHostState
) {
    val message = context.getString(resId)
    snackbarHostState.showSnackbar(message = message)
}

private data class ProfileRow(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val titleRes: Int,
    val intent: ProfileUiIntent,
    val trailingRes: Int? = null,
    val isDestructive: Boolean = false,
)

private fun profileRows(currencyRes: Int) = listOf(
    ProfileRow(Icons.Outlined.List, R.string.profile_my_orders, ProfileUiIntent.OnMyOrdersClicked),
    ProfileRow(Icons.Outlined.LocationOn, R.string.profile_saved_addresses, ProfileUiIntent.OnSavedAddressesClicked),
    ProfileRow(Icons.Outlined.CreditCard, R.string.profile_currency, ProfileUiIntent.OnCurrencyClicked, currencyRes),
    ProfileRow(Icons.Outlined.Settings, R.string.profile_settings, ProfileUiIntent.OnSettingsClicked),
    ProfileRow(Icons.Outlined.ExitToApp, R.string.profile_logout, ProfileUiIntent.OnLogoutClicked, isDestructive = true),
)
