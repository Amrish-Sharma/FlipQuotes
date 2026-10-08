package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.ui.theme.AppTheme
import com.app.codebuzz.flipquotes.ui.theme.PlayfairDisplayFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    theme: AppTheme,
    isRefreshing: Boolean = false,
    onRefreshClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PlayfairDisplayFont
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.menu)
                )
            }
        },
        actions = {
            IconButton(onClick = onRefreshClick, enabled = !isRefreshing) {
                // Spin for as long as the refresh is actually running
                val rotation = if (isRefreshing) {
                    val spin by rememberInfiniteTransition(label = "refresh").animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
                        label = "refresh-rotation"
                    )
                    spin
                } else {
                    0f
                }
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh_quotes),
                    tint = theme.onPrimaryColor,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = theme.primaryColor,
            titleContentColor = theme.onPrimaryColor,
            actionIconContentColor = theme.onPrimaryColor,
            navigationIconContentColor = theme.onPrimaryColor
        )
    )
}
