package com.example.wearzone.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.R
import com.example.wearzone.presentation.common.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(id = R.string.content_desc_menu),
                    tint = AppTheme.colors.textPrimary,
                )
            }
        },
        actions = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = stringResource(id = R.string.content_desc_cart),
                        tint = AppTheme.colors.textPrimary,
                    )
                }
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp),
                    containerColor = AppTheme.colors.selected,
                    contentColor = AppTheme.colors.onAccent,
                ) {
                    Text(text = stringResource(id = R.string.dummy_cart_count))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.background,
        )
    )
}

@Composable
fun GreetingSection(
    userName : String
) {
    Column {
        Text(
            text = stringResource(id = R.string.home_greeting, userName),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.textPrimary,
        )
        Text(
            text = stringResource(id = R.string.home_subtitle),
            fontSize = 16.sp,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )    }
}



