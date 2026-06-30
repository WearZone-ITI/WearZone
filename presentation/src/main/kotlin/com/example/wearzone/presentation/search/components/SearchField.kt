package com.example.wearzone.presentation.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppColors
import com.example.wearzone.presentation.search.SearchUiIntent
import com.example.wearzone.presentation.search.SearchUiState

@Composable
fun SearchField(
    state: SearchUiState,
    onIntent: (SearchUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onIntent(SearchUiIntent.OnQueryChanged(it)) },
            modifier = Modifier.weight(1f),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.content_desc_search),
                )
            },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    IconButton(onClick = { onIntent(SearchUiIntent.OnQueryChanged("")) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.content_desc_clear_search),
                        )
                    }
                }
            },
            placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onIntent(SearchUiIntent.OnSearchSubmitted) }),
        )
        IconButton(
            onClick = { onIntent(SearchUiIntent.OnFilterClicked) },
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(AppColors.Primary),
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(id = R.string.search_filters),
                tint = AppColors.OnPrimary,
            )
        }
    }
}