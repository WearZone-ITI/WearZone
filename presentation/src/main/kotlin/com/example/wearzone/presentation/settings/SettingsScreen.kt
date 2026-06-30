package com.example.wearzone.presentation.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.presentation.R
import com.example.wearzone.domain.settings.model.ThemeMode
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.settings.components.SettingsDivider
import com.example.wearzone.presentation.settings.components.SettingsPreferenceRow
import com.example.wearzone.presentation.settings.components.SettingsSection
import com.example.wearzone.presentation.settings.components.SettingsSwitchRow
import com.example.wearzone.presentation.settings.components.ThemeModeRow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    appVersion: String,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                SettingsUiEffect.NavigateBack -> onNavigateBack()
                SettingsUiEffect.ShowLanguagePicker -> showLanguageDialog = true
                SettingsUiEffect.ShowLanguageUpdated -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(currentContext.getString(R.string.settings_language_updated))
                }
                SettingsUiEffect.ShowPrivacyPolicy -> showPrivacyPolicyDialog = true
                SettingsUiEffect.ShowTerms -> showTermsDialog = true
                is SettingsUiEffect.ShowError -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(currentContext.getString(effect.messageRes))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppTheme.colors.background,
    ) { innerPadding ->
        SettingsContent(
            uiState = uiState,
            appVersion = appVersion,
            onIntent = viewModel::handleIntent,
            modifier = Modifier.padding(innerPadding),
        )
    }

    val contentState = uiState as? SettingsUiState.Content
    if (showLanguageDialog && contentState != null) {
        LanguagePickerDialog(
            selectedLanguageCode = contentState.languageCode,
            onLanguageSelected = { languageCode ->
                showLanguageDialog = false
                viewModel.handleIntent(SettingsUiIntent.OnLanguageSelected(languageCode))
            },
            onDismiss = { showLanguageDialog = false },
        )
    }

    if (showPrivacyPolicyDialog) {
        LegalInfoDialog(
            titleRes = R.string.settings_privacy_policy,
            bodyRes = R.string.settings_privacy_policy_body,
            onDismiss = { showPrivacyPolicyDialog = false },
        )
    }

    if (showTermsDialog) {
        LegalInfoDialog(
            titleRes = R.string.settings_terms_conditions,
            bodyRes = R.string.settings_terms_conditions_body,
            onDismiss = { showTermsDialog = false },
        )
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    appVersion: String,
    onIntent: (SettingsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        when (uiState) {
            SettingsUiState.Loading -> CircularProgressIndicator(
                color = AppTheme.colors.selected,
                modifier = Modifier.align(Alignment.Center),
            )

            is SettingsUiState.Error -> Text(
                text = stringResource(uiState.messageRes),
                color = AppTheme.colors.error,
                modifier = Modifier.align(Alignment.Center),
            )

            is SettingsUiState.Content -> SettingsLoadedContent(
                uiState = uiState,
                appVersion = appVersion,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun SettingsLoadedContent(
    uiState: SettingsUiState.Content,
    appVersion: String,
    onIntent: (SettingsUiIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
    ) {
        item {
            SettingsTopBar(
                onBackClicked = { onIntent(SettingsUiIntent.OnBackClicked) },
            )
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
        item {
            SettingsSection(titleRes = R.string.settings_appearance_section) {
                ThemeModeRow(
                    titleRes = R.string.settings_theme_system,
                    selected = uiState.selectedThemeMode == ThemeMode.SystemDefault,
                    onClick = { onIntent(SettingsUiIntent.OnThemeModeSelected(ThemeMode.SystemDefault)) },
                )
                SettingsDivider()
                ThemeModeRow(
                    titleRes = R.string.settings_theme_light,
                    selected = uiState.selectedThemeMode == ThemeMode.Light,
                    onClick = { onIntent(SettingsUiIntent.OnThemeModeSelected(ThemeMode.Light)) },
                )
                SettingsDivider()
                ThemeModeRow(
                    titleRes = R.string.settings_theme_dark,
                    selected = uiState.selectedThemeMode == ThemeMode.Dark,
                    onClick = { onIntent(SettingsUiIntent.OnThemeModeSelected(ThemeMode.Dark)) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
        item {
            SettingsSection(titleRes = R.string.settings_preferences_section) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Notifications,
                    titleRes = R.string.settings_notifications,
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { onIntent(SettingsUiIntent.OnNotificationsToggled(it)) },
                )
                SettingsDivider()
                SettingsPreferenceRow(
                    icon = Icons.Outlined.Language,
                    titleRes = R.string.settings_language,
                    valueRes = uiState.languageRes,
                    onClick = { onIntent(SettingsUiIntent.OnLanguageClicked) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
        item {
            SettingsSection(titleRes = R.string.settings_about_section) {
                SettingsPreferenceRow(
                    icon = Icons.Outlined.Info,
                    titleRes = R.string.settings_privacy_policy,
                    onClick = { onIntent(SettingsUiIntent.OnPrivacyPolicyClicked) },
                )
                SettingsDivider()
                SettingsPreferenceRow(
                    icon = Icons.Outlined.Info,
                    titleRes = R.string.settings_terms_conditions,
                    onClick = { onIntent(SettingsUiIntent.OnTermsClicked) },
                )
                SettingsDivider()
                SettingsPreferenceRow(
                    icon = Icons.Outlined.Info,
                    titleRes = R.string.settings_app_version,
                    valueText = appVersion,
                    showChevron = false,
                )
            }
        }
    }
}

@Composable
private fun SettingsTopBar(
    onBackClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.settings_back_content_description),
                tint = AppTheme.colors.textPrimary,
            )
        }
        Text(
            text = stringResource(R.string.profile_brand_title),
            style = MaterialTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Outlined.ShoppingBag,
                contentDescription = stringResource(R.string.content_desc_cart),
                tint = AppTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.settings_language_dialog_title)) },
        text = {
            Column {
                ThemeModeRow(
                    titleRes = R.string.settings_language_english,
                    selected = selectedLanguageCode == ENGLISH_LANGUAGE_CODE,
                    onClick = { onLanguageSelected(ENGLISH_LANGUAGE_CODE) },
                )
                SettingsDivider()
                ThemeModeRow(
                    titleRes = R.string.settings_language_arabic,
                    selected = selectedLanguageCode == ARABIC_LANGUAGE_CODE,
                    onClick = { onLanguageSelected(ARABIC_LANGUAGE_CODE) },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.profile_logout_dialog_cancel))
            }
        },
        containerColor = AppTheme.colors.surface,
    )
}

@Composable
private fun LegalInfoDialog(
    titleRes: Int,
    bodyRes: Int,
    onDismiss: () -> Unit,
) {
    // TODO: Replace this temporary copy with approved WearZone legal content.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(titleRes)) },
        text = {
            Text(
                text = stringResource(bodyRes),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.settings_dialog_done))
            }
        },
        containerColor = AppTheme.colors.surface,
        titleContentColor = AppTheme.colors.textPrimary,
        textContentColor = AppTheme.colors.textSecondary,
    )
}

private const val ENGLISH_LANGUAGE_CODE = "en"
private const val ARABIC_LANGUAGE_CODE = "ar"
