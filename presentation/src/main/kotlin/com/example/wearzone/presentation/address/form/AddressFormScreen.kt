package com.example.wearzone.presentation.address.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Signpost
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.common.SignInRequiredDialog
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun AddressFormScreen(
    addressId: Long?,
    onNavigateBack: () -> Unit,
    onAddressSaved: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: AddressFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(addressId) {
        viewModel.handleIntent(AddressFormUiIntent.OnInitialize(addressId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                AddressFormUiEffect.AddressSaved -> onAddressSaved()
                AddressFormUiEffect.NavigateBack -> onNavigateBack()
                is AddressFormUiEffect.ShowMessage -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AddressFormTopBar(
                titleRes = if (addressId != null) R.string.address_edit_title else R.string.address_add_title,
                onBackClicked = { viewModel.handleIntent(AddressFormUiIntent.OnBackClicked) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        AddressFormContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister,
            onContinueBrowsing = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun AddressFormContent(
    uiState: AddressFormUiState,
    onIntent: (AddressFormUiIntent) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueBrowsing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = AppTheme.colors.selected,
                modifier = Modifier.align(Alignment.Center),
            )
        } else if (uiState.isSignInRequired) {
            SignInRequiredDialog(
                onSignInRegister = onNavigateToLogin,
                onContinueBrowsing = onContinueBrowsing,
            )
        } else {
            AddressFormLoadedContent(
                uiState = uiState,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun AddressFormLoadedContent(
    uiState: AddressFormUiState,
    onIntent: (AddressFormUiIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        uiState.screenError?.let { messageRes ->
            item {
                Text(
                    text = stringResource(messageRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            AddressFormSection(titleRes = R.string.address_contact_information)
        }
        item {
            AddressTextField(
                value = uiState.recipientName,
                onValueChange = { onIntent(AddressFormUiIntent.OnRecipientNameChanged(it)) },
                labelRes = R.string.address_recipient_name,
                icon = Icons.Outlined.Person,
                errorRes = uiState.recipientNameError,
                placeholderRes = R.string.address_recipient_name_hint,
            )
        }
        item {
            AddressTextField(
                value = uiState.phone,
                onValueChange = { onIntent(AddressFormUiIntent.OnPhoneChanged(it)) },
                labelRes = R.string.address_mobile_number,
                icon = Icons.Outlined.PhoneIphone,
                errorRes = uiState.phoneError,
                keyboardType = KeyboardType.Phone,
                placeholderRes = R.string.address_mobile_number_hint,
                prefix = uiState.countryCode,
            )
        }
        item {
            HorizontalDivider(color = AppTheme.colors.divider)
        }
        item {
            AddressFormSection(titleRes = R.string.address_delivery_address)
        }
        item {
            AddressTextField(
                value = uiState.country,
                onValueChange = { onIntent(AddressFormUiIntent.OnCountryChanged(it)) },
                labelRes = R.string.address_country,
                icon = Icons.Outlined.Public,
                errorRes = uiState.countryError,
                placeholderRes = R.string.address_country_hint,
            )
        }
        item {
            AddressTextField(
                value = uiState.province,
                onValueChange = { onIntent(AddressFormUiIntent.OnProvinceChanged(it)) },
                labelRes = R.string.address_province,
                icon = Icons.Outlined.Apartment,
                placeholderRes = R.string.address_province_hint,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AddressTextField(
                    value = uiState.city,
                    onValueChange = { onIntent(AddressFormUiIntent.OnCityChanged(it)) },
                    labelRes = R.string.address_city,
                    icon = Icons.Outlined.LocationCity,
                    errorRes = uiState.cityError,
                    placeholderRes = R.string.address_city_hint,
                    modifier = Modifier.weight(1f),
                )
                AddressTextField(
                    value = uiState.zip,
                    onValueChange = { onIntent(AddressFormUiIntent.OnZipChanged(it)) },
                    labelRes = R.string.address_postal_code,
                    icon = Icons.Outlined.Signpost,
                    errorRes = uiState.zipError,
                    placeholderRes = R.string.address_postal_code_hint,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            AddressTextField(
                value = uiState.address1,
                onValueChange = { onIntent(AddressFormUiIntent.OnAddress1Changed(it)) },
                labelRes = R.string.address_street,
                icon = Icons.Outlined.Place,
                errorRes = uiState.address1Error,
                placeholderRes = R.string.address_street_hint,
                minLines = 2,
            )
        }
        item {
            AddressTextField(
                value = uiState.address2,
                onValueChange = { onIntent(AddressFormUiIntent.OnAddress2Changed(it)) },
                labelRes = R.string.address_address2,
                icon = Icons.Outlined.Home,
                placeholderRes = R.string.address_address2_hint,
            )
        }
        item {
            AddressTextField(
                value = uiState.company,
                onValueChange = { onIntent(AddressFormUiIntent.OnCompanyChanged(it)) },
                labelRes = R.string.address_company,
                icon = Icons.Outlined.Apartment,
                placeholderRes = R.string.address_company_hint,
            )
        }
        item {
            DefaultAddressRow(
                checked = uiState.isDefault,
                onCheckedChange = { onIntent(AddressFormUiIntent.OnDefaultChanged(it)) },
            )
        }
        item {
            SaveAddressButton(
                uiState = uiState,
                onClick = { onIntent(AddressFormUiIntent.OnSaveClicked) },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddressFormTopBar(
    titleRes: Int,
    onBackClicked: () -> Unit,
) {
    TopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_back),
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
private fun AddressFormSection(titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colors.textSecondary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun AddressTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    errorRes: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    placeholderRes: Int? = null,
    readOnly: Boolean = false,
    prefix: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = { Text(text = stringResource(labelRes)) },
        placeholder = {
            placeholderRes?.let { Text(text = stringResource(it)) }
        },
        prefix = {
            prefix?.let {
                Text(
                    text = it,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(labelRes),
            )
        },
        isError = errorRes != null,
        supportingText = {
            errorRes?.let { Text(text = stringResource(it)) }
        },
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTheme.colors.selected,
            unfocusedBorderColor = AppTheme.colors.divider,
            focusedContainerColor = AppTheme.colors.surface,
            unfocusedContainerColor = AppTheme.colors.surfaceVariant,
            errorBorderColor = AppTheme.colors.error,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun DefaultAddressRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.address_set_as_default),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.address_set_as_default_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppTheme.colors.onAccent,
                checkedTrackColor = AppTheme.colors.selected,
            ),
        )
    }
}

@Composable
private fun SaveAddressButton(
    uiState: AddressFormUiState,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = uiState.canSubmit,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.selected,
            contentColor = AppTheme.colors.onAccent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        if (uiState.isSubmitting) {
            CircularProgressIndicator(
                color = AppTheme.colors.onAccent,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.address_save),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
