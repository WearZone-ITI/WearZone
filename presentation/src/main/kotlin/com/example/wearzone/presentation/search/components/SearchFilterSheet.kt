package com.example.wearzone.presentation.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme
import com.example.wearzone.presentation.search.SearchFilterOptionUiModel
import com.example.wearzone.presentation.search.SearchUiIntent
import com.example.wearzone.presentation.search.SearchUiState
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterSheet(
    state: SearchUiState,
    onIntent: (SearchUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = { onIntent(SearchUiIntent.OnDismissFilters) },
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        contentColor = AppTheme.colors.textPrimary,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = AppTheme.colors.border)
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.search_filters),
                    style = MaterialTheme.typography.titleLarge,
                    color = AppTheme.colors.textPrimary,
                )
            }
            item { PriceFilterRow(state = state, onIntent = onIntent) }
            item {
                FilterOptions(
                    title = stringResource(id = R.string.search_filter_brand),
                    options = state.brands,
                    selectedTitle = state.selectedBrandTitle,
                    onSelected = { onIntent(SearchUiIntent.OnBrandSelected(it)) },
                )
            }
            item {
                FilterOptions(
                    title = stringResource(id = R.string.search_filter_category),
                    options = state.categories,
                    selectedTitle = state.selectedCategoryTitle,
                    onSelected = { onIntent(SearchUiIntent.OnCategorySelected(it)) },
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = { onIntent(SearchUiIntent.OnResetFilters) },
                        modifier = Modifier.weight(1f),
                    ) { Text(text = stringResource(id = R.string.search_reset_filters)) }
                    Button(
                        onClick = { onIntent(SearchUiIntent.OnApplyFilters) },
                        modifier = Modifier.weight(1f),
                    ) { Text(text = stringResource(id = R.string.search_apply_filters)) }
                }
            }
        }
    }
}

@Composable
private fun PriceFilterRow(
    state: SearchUiState,
    onIntent: (SearchUiIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(id = R.string.search_filter_price),
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.minPrice,
                onValueChange = { onIntent(SearchUiIntent.OnMinPriceChanged(it)) },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(id = R.string.search_min_price)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = filterTextFieldColors(),
            )
            OutlinedTextField(
                value = state.maxPrice,
                onValueChange = { onIntent(SearchUiIntent.OnMaxPriceChanged(it)) },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(id = R.string.search_max_price)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = filterTextFieldColors(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterOptions(
    title: String,
    options: ImmutableList<SearchFilterOptionUiModel>,
    selectedTitle: String?,
    onSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AssistChip(
                onClick = { onSelected(null) },
                label = { Text(text = stringResource(id = R.string.search_filter_all)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = AppTheme.colors.surfaceVariant,
                    labelColor = AppTheme.colors.textPrimary,
                ),
            )
            options.forEach { option ->
                val selected = selectedTitle == option.title
                FilterChip(
                    selected = selected,
                    onClick = { onSelected(option.title) },
                    label = { Text(text = option.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = AppTheme.colors.surfaceVariant,
                        labelColor = AppTheme.colors.textPrimary,
                        selectedContainerColor = AppTheme.colors.selected,
                        selectedLabelColor = AppTheme.colors.onAccent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun filterTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppTheme.colors.textPrimary,
    unfocusedTextColor = AppTheme.colors.textPrimary,
    focusedLabelColor = AppTheme.colors.selected,
    unfocusedLabelColor = AppTheme.colors.textSecondary,
    focusedContainerColor = AppTheme.colors.surfaceVariant,
    unfocusedContainerColor = AppTheme.colors.surfaceVariant,
    disabledContainerColor = AppTheme.colors.surfaceVariant,
    cursorColor = AppTheme.colors.selected,
    focusedBorderColor = AppTheme.colors.selected,
    unfocusedBorderColor = AppTheme.colors.border,
)
