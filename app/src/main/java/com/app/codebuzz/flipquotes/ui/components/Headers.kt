package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    modifier: Modifier = Modifier,
    onRefreshClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = "FlipQuotes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.playfair_display)) // Use playfair display One for elegant header
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh quotes"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}
