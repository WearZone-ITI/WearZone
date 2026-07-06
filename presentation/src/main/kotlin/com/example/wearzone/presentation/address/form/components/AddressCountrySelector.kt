package com.example.wearzone.presentation.address.form.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.address.form.CountryUiModel
import com.example.wearzone.presentation.common.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddressCountrySelector(
    countries: ImmutableList<CountryUiModel>,
    selectedCountry: CountryUiModel?,
    errorRes: Int?,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    openSelectorSignal: Int = 0,
) {
    var isSheetVisible by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    if (openSelectorSignal > 0) {
        LaunchedEffect(openSelectorSignal) {
            isSheetVisible = true
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCountry?.let { "${it.name} (${it.dialCode})" }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.address_country)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Public,
                    contentDescription = stringResource(R.string.address_country),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = stringResource(R.string.address_select_country),
                )
            },
            isError = errorRes != null,
            supportingText = { errorRes?.let { Text(text = stringResource(it)) } },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppTheme.colors.selected,
                unfocusedBorderColor = AppTheme.colors.divider,
                focusedContainerColor = AppTheme.colors.surface,
                unfocusedContainerColor = AppTheme.colors.surfaceVariant,
                errorBorderColor = AppTheme.colors.error,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .semantics { role = Role.Button }
                .clickable { isSheetVisible = true },
        )
    }

    if (isSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isSheetVisible = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppTheme.colors.surface,
        ) {
            val filteredCountries = remember(query, countries) {
                countries.filter { country ->
                    country.name.contains(query, ignoreCase = true) ||
                            country.isoCode.contains(query, ignoreCase = true) ||
                            country.dialCode.contains(query)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.address_select_country),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(text = stringResource(R.string.address_country_search)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.selected,
                        unfocusedBorderColor = AppTheme.colors.divider,
                        focusedContainerColor = AppTheme.colors.surface,
                        unfocusedContainerColor = AppTheme.colors.surfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (filteredCountries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.address_country_no_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        items(filteredCountries, key = { it.isoCode }) { country ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = country.name,
                                        color = AppTheme.colors.textPrimary,
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = "${country.isoCode} ${country.dialCode}",
                                        color = AppTheme.colors.textSecondary,
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCountrySelected(country.isoCode)
                                        isSheetVisible = false
                                        query = ""
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}
