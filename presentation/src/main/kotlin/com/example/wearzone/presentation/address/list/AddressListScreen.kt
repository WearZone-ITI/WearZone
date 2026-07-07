package com.example.wearzone.presentation.address.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.address.list.components.AddressCard
import com.example.wearzone.presentation.address.list.components.AddressDeleteConfirmationDialog
import com.example.wearzone.presentation.common.NetworkErrorState
import com.example.wearzone.presentation.common.PremiumEmptyState
import com.example.wearzone.presentation.common.SignInRequiredDialog
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun AddressListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    onNavigateToEditAddress: (Long) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    refreshAfterChange: Boolean = false,
    onRefreshAfterChangeConsumed: () -> Unit = {},
    viewModel: AddressListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var pendingDeleteAddressId by remember { mutableStateOf<Long?>(null) }
    var hasPassedInitialResume by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (hasPassedInitialResume) {
                    viewModel.handleIntent(AddressListUiIntent.OnRefresh)
                } else {
                    hasPassedInitialResume = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(refreshAfterChange) {
        if (refreshAfterChange) {
            viewModel.handleIntent(AddressListUiIntent.OnRefresh)
            onRefreshAfterChangeConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                AddressListUiEffect.NavigateBack -> onNavigateBack()
                AddressListUiEffect.NavigateToAddAddress -> onNavigateToAddAddress()
                is AddressListUiEffect.NavigateToEditAddress -> onNavigateToEditAddress(effect.addressId)
                is AddressListUiEffect.ShowDeleteConfirmation -> {
                    pendingDeleteAddressId = effect.addressId
                }
                is AddressListUiEffect.ShowMessage -> {
                    val message = context.getString(effect.messageRes)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AddressListTopBar(onIntent = viewModel::handleIntent)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        AddressListContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent,
            onNavigateToLogin = onNavigateToLogin,
            onContinueBrowsing = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }

    pendingDeleteAddressId?.let { addressId ->
        AddressDeleteConfirmationDialog(
            onConfirm = {
                pendingDeleteAddressId = null
                viewModel.handleIntent(AddressListUiIntent.OnDeleteConfirmed(addressId))
            },
            onDismiss = { pendingDeleteAddressId = null },
        )
    }
}

@Composable
private fun AddressListContent(
    uiState: AddressListUiState,
    onIntent: (AddressListUiIntent) -> Unit,
    onNavigateToLogin: () -> Unit,
    onContinueBrowsing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        when (uiState) {
            AddressListUiState.Loading -> CircularProgressIndicator(
                color = AppTheme.colors.selected,
                modifier = Modifier.align(Alignment.Center),
            )
            AddressListUiState.Empty -> {
                EmptyAddressesContent(onAddClicked = { onIntent(AddressListUiIntent.OnAddClicked) })
            }
            AddressListUiState.SignInRequired -> {
                SignInRequiredDialog(
                    onSignInRegister = onNavigateToLogin,
                    onContinueBrowsing = onContinueBrowsing,
                )
            }
            is AddressListUiState.Error -> {
                NetworkErrorState(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = { onIntent(AddressListUiIntent.OnRetry) },
                )
            }
            is AddressListUiState.Content -> {
                AddressCardsContent(
                    uiState = uiState,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddressListTopBar(
    onIntent: (AddressListUiIntent) -> Unit,
) {
    TopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Text(
                text = stringResource(R.string.address_list_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = { onIntent(AddressListUiIntent.OnBackClicked) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back),
                    tint = AppTheme.colors.textPrimary,
                )
            }
        },
        actions = {
            IconButton(onClick = { onIntent(AddressListUiIntent.OnRefresh) }) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.address_refresh),
                    tint = AppTheme.colors.textPrimary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.background,
        ),
    )
}

@Composable
private fun AddressCardsContent(
    uiState: AddressListUiState.Content,
    onIntent: (AddressListUiIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.address_list_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (uiState.isRefreshing || uiState.isMutating) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = AppTheme.colors.selected)
                }
            }
        }
        items(uiState.addresses, key = { it.id }) { address ->
            AddressCard(
                address = address,
                isActionEnabled = !uiState.isMutating,
                onEditClicked = { onIntent(AddressListUiIntent.OnEditClicked(address.id)) },
                onDeleteClicked = { onIntent(AddressListUiIntent.OnDeleteClicked(address)) },
                onSetDefaultClicked = { onIntent(AddressListUiIntent.OnSetDefaultClicked(address.id)) },
            )
        }
        item {
            AddressPrimaryButton(
                textRes = R.string.address_add_new,
                onClick = { onIntent(AddressListUiIntent.OnAddClicked) },
                enabled = !uiState.isMutating,
            )
        }
    }
}

@Composable
private fun EmptyAddressesContent(
    onAddClicked: () -> Unit,
) {
    PremiumEmptyState(
        lottieResId = R.raw.no_address,
        title = stringResource(R.string.address_empty_title),
        description = stringResource(R.string.address_empty_subtitle),
        buttonText = stringResource(R.string.address_add_new),
        onButtonClick = onAddClicked,
    )
}

@Composable
private fun AddressPrimaryButton(
    textRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.selected,
            contentColor = AppTheme.colors.onAccent,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = stringResource(textRes),
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(
            text = stringResource(textRes),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
