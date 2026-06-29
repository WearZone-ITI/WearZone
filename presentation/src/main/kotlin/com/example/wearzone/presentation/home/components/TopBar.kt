package com.example.wearzone.presentation.home.components

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.R

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
                    color = Color.Black
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(id = R.string.content_desc_menu),
                    tint = Color.Black
                )
            }
        },
        actions = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = stringResource(id = R.string.content_desc_cart),
                        tint = Color.Black
                    )
                }
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp),
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    Text(text = stringResource(id = R.string.dummy_cart_count))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.top_bar_background)
        )
    )
}

@Composable
fun GreetingSection(
    userName : String
) {
    Log.e("name",userName)
    Column {
        Text(
            text = stringResource(id = R.string.home_greeting, userName),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Text(
            text = stringResource(id = R.string.home_subtitle),
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )    }
}



