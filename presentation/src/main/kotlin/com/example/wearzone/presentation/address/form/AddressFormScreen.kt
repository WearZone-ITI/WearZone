package com.example.wearzone.presentation.address.form

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Signpost
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.presentation.address.form.components.AddressCountrySelector
import com.example.wearzone.presentation.address.form.components.AddressMapPickerDialog
import com.example.wearzone.presentation.address.form.components.AddressMapPreviewCard
import com.example.wearzone.presentation.common.SignInRequiredDialog
import com.example.wearzone.presentation.common.rememberKeyboardVisibility
import com.example.wearzone.presentation.common.theme.AppTheme
import java.util.Locale
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
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        viewModel.handleIntent(
            AddressFormUiIntent.OnLocationPermissionResult(
                isGranted = permissions.values.any { it },
            ),
        )
    }
    val requestLocationPermission = {
        if (context.hasAddressLocationPermission()) {
            viewModel.handleIntent(AddressFormUiIntent.OnLocationPermissionResult(isGranted = true))
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(addressId) {
        viewModel.handleIntent(AddressFormUiIntent.OnInitialize(addressId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                AddressFormUiEffect.AddressSaved -> onAddressSaved()
                AddressFormUiEffect.NavigateBack -> onNavigateBack()
                AddressFormUiEffect.RequestLocationPermission -> requestLocationPermission()
                is AddressFormUiEffect.ShowMessage -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
                }
            }
        }
    }

    val isKeyboardVisible by rememberKeyboardVisibility()
    var isAnyFormFieldFocused by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AddressFormTopBar(
                titleRes = if (addressId != null) R.string.address_edit_title else R.string.address_add_title,
                onBackClicked = { viewModel.handleIntent(AddressFormUiIntent.OnBackClicked) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isKeyboardVisible && !isAnyFormFieldFocused && !uiState.isLoading && !uiState.isSignInRequired) {
                StickySaveAddressBar(
                    uiState = uiState,
                    onClick = { viewModel.handleIntent(AddressFormUiIntent.OnSaveClicked) },
                )
            }
        },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        AddressFormContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToRegister = onNavigateToRegister,
            onContinueBrowsing = onNavigateBack,
            onFormFieldFocusChanged = { isAnyFormFieldFocused = it },
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (uiState.isMapPickerVisible) {
        AddressMapPickerDialog(
            latitude = uiState.mapPickerLatitude ?: DEFAULT_MAP_LATITUDE,
            longitude = uiState.mapPickerLongitude ?: DEFAULT_MAP_LONGITUDE,
            currentLatitude = uiState.mapPickerCurrentLatitude,
            currentLongitude = uiState.mapPickerCurrentLongitude,
            isResolving = uiState.isResolvingMapLocation,
            selectedLocation = uiState.mapPickerSelectedLocation,
            searchQuery = uiState.addressSearchQuery,
            suggestions = uiState.addressSuggestions,
            isSearching = uiState.isSearchingAddress,
            messageRes = uiState.addressLookupMessage,
            isResolvingCurrentLocation = uiState.isResolvingCurrentLocation,
            onSearchQueryChanged = { query ->
                viewModel.handleIntent(AddressFormUiIntent.OnAddressSearchQueryChanged(query))
            },
            onSuggestionSelected = { suggestionId ->
                viewModel.handleIntent(AddressFormUiIntent.OnAddressSuggestionSelected(suggestionId))
            },
            onSearchCleared = {
                viewModel.handleIntent(AddressFormUiIntent.OnMapPickerSearchCleared)
            },
            onUseCurrentLocation = {
                viewModel.handleIntent(AddressFormUiIntent.OnUseCurrentLocationClicked)
            },
            onCoordinatesChanged = { latitude, longitude ->
                viewModel.handleIntent(
                    AddressFormUiIntent.OnMapPickerCoordinatesChanged(latitude, longitude),
                )
            },
            onConfirm = {
                viewModel.handleIntent(AddressFormUiIntent.OnConfirmMapLocationClicked)
            },
            onDismiss = {
                viewModel.handleIntent(AddressFormUiIntent.OnDismissMapPicker)
            },
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
    onFormFieldFocusChanged: (Boolean) -> Unit,
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
                onFormFieldFocusChanged = onFormFieldFocusChanged,
            )
        }
    }
}

@Composable
private fun AddressFormLoadedContent(
    uiState: AddressFormUiState,
    onIntent: (AddressFormUiIntent) -> Unit,
    onFormFieldFocusChanged: (Boolean) -> Unit,
) {
    var countrySelectorOpenSignal by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                onFocusChanged = onFormFieldFocusChanged,
            )
        }
        item {
            PremiumPhoneField(
                value = uiState.phone,
                onValueChange = { onIntent(AddressFormUiIntent.OnPhoneChanged(it)) },
                countryIsoCode = uiState.selectedCountry?.isoCode ?: uiState.countryIsoCode,
                dialCode = uiState.selectedCountry?.dialCode ?: uiState.countryDialCode,
                errorRes = uiState.phoneError,
                placeholderRes = R.string.address_mobile_number_hint,
                onCountryPrefixClicked = { countrySelectorOpenSignal++ },
                onFocusChanged = onFormFieldFocusChanged,
            )
        }
        item {
            HorizontalDivider(color = AppTheme.colors.divider)
        }
        item {
            AddressFormSection(titleRes = R.string.address_delivery_address)
        }
        item {
            AddressCountrySelector(
                countries = uiState.countries,
                selectedCountry = uiState.selectedCountry,
                errorRes = uiState.countryError,
                openSelectorSignal = countrySelectorOpenSignal,
                onCountrySelected = { onIntent(AddressFormUiIntent.OnCountrySelected(it)) },
            )
        }
        if (uiState.selectedLocation == null) {
            item {
                ChooseDeliveryLocationCard(
                    onClick = { onIntent(AddressFormUiIntent.OnPickOnMapClicked) },
                )
            }
        } else {
            uiState.selectedLocation?.let { selectedLocation ->
                item {
                    AddressMapPreviewCard(
                        selectedLocation = selectedLocation,
                        onChangeOnMap = { onIntent(AddressFormUiIntent.OnPickOnMapClicked) },
                        onClear = { onIntent(AddressFormUiIntent.OnClearSelectedLocationClicked) },
                    )
                }
            }
        }
        item {
            HorizontalDivider(color = AppTheme.colors.divider)
        }
        item {
            AddressFormSection(titleRes = R.string.address_details)
        }
        item {
            AddressTextField(
                value = uiState.province,
                onValueChange = { onIntent(AddressFormUiIntent.OnProvinceChanged(it)) },
                labelRes = R.string.address_province,
                icon = Icons.Outlined.Apartment,
                placeholderRes = R.string.address_province_hint,
                onFocusChanged = onFormFieldFocusChanged,
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
                    onFocusChanged = onFormFieldFocusChanged,
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
                    onFocusChanged = onFormFieldFocusChanged,
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
                onFocusChanged = onFormFieldFocusChanged,
            )
        }
        item {
            HorizontalDivider(color = AppTheme.colors.divider)
        }
        item {
            AddressFormSection(titleRes = R.string.address_optional)
        }
        item {
            AddressTextField(
                value = uiState.address2,
                onValueChange = { onIntent(AddressFormUiIntent.OnAddress2Changed(it)) },
                labelRes = R.string.address_address2,
                icon = Icons.Outlined.Home,
                placeholderRes = R.string.address_address2_hint,
                onFocusChanged = onFormFieldFocusChanged,
            )
        }
        item {
            AddressTextField(
                value = uiState.company,
                onValueChange = { onIntent(AddressFormUiIntent.OnCompanyChanged(it)) },
                labelRes = R.string.address_company,
                icon = Icons.Outlined.Apartment,
                placeholderRes = R.string.address_company_hint,
                onFocusChanged = onFormFieldFocusChanged,
            )
        }
        item {
            DefaultAddressRow(
                checked = uiState.isDefault,
                onCheckedChange = { onIntent(AddressFormUiIntent.OnDefaultChanged(it)) },
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
private fun PremiumPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    countryIsoCode: String,
    dialCode: String,
    errorRes: Int?,
    placeholderRes: Int,
    onCountryPrefixClicked: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (errorRes != null) AppTheme.colors.error else AppTheme.colors.divider
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.address_mobile_number),
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textSecondary,
            fontWeight = FontWeight.SemiBold,
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppTheme.colors.surface,
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onCountryPrefixClicked)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = countryIsoCode.toFlagEmoji(),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = dialCode,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = stringResource(R.string.address_select_country),
                        tint = AppTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(width = 1.dp, height = 28.dp)
                        .background(AppTheme.colors.divider),
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = AppTheme.colors.textPrimary,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    cursorBrush = SolidColor(AppTheme.colors.selected),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isBlank()) {
                                Text(
                                    text = stringResource(placeholderRes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AppTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                )
            }
        }
        errorRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.error,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun ChooseDeliveryLocationCard(
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.AddLocationAlt,
                contentDescription = stringResource(R.string.address_choose_delivery_location),
                tint = AppTheme.colors.selected,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.address_choose_delivery_location),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.address_choose_delivery_location_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
        }
    }
}

private fun Context.hasAddressLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

@Composable
private fun StickySaveAddressBar(
    uiState: AddressFormUiState,
    onClick: () -> Unit,
) {
    Surface(
        color = AppTheme.colors.surface,
        tonalElevation = 4.dp,
    ) {
        SaveAddressButton(
            uiState = uiState,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
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
    onFocusChanged: (Boolean) -> Unit = {},
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
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChanged(it.isFocused) },
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
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = uiState.canSubmit,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.selected,
            contentColor = AppTheme.colors.onAccent,
        ),
        modifier = modifier
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

private const val DEFAULT_MAP_LATITUDE = 30.0444
private const val DEFAULT_MAP_LONGITUDE = 31.2357
private const val REGIONAL_INDICATOR_OFFSET = 0x1F1E6

private fun String.toFlagEmoji(): String {
    val normalizedCode = trim().uppercase(Locale.US)
    if (normalizedCode.length != 2 || normalizedCode.any { it !in 'A'..'Z' }) return ""
    return normalizedCode
        .map { Character.toChars(REGIONAL_INDICATOR_OFFSET + (it.code - 'A'.code)).concatToString() }
        .joinToString(separator = "")
}
