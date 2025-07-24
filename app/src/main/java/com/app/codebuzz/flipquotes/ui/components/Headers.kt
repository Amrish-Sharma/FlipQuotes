package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.ui.theme.AppTheme
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(
    theme: AppTheme,
    isFlipMode: Boolean = false,
    onRefreshClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onFlipToggle: () -> Unit = {}
) {
    val context = LocalContext.current
    var isRotating by remember { mutableStateOf(false) }
    var playSound by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isRotating) 60f else 0f,
        animationSpec = tween(durationMillis = 300), label = "refresh-rotation"
    )
    // Play sound effect when playSound is true
    LaunchedEffect(playSound) {
        if (playSound) {
            try {
                // Uncomment the next line if you have refresh_sound in res/raw
                // val player = android.media.MediaPlayer.create(context, R.raw.refresh_sound)
                // player?.setOnCompletionListener { it.release() }
                // player?.start()
            } catch (e: Exception) {
e.printStackTrace()
            }
            playSound = false
        }
    }
    TopAppBar(
        title = {
            Text(
                text = "FlipQuotes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.playfair_display))
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
            IconButton(onClick = onFlipToggle) {
                Icon(
                    imageVector = if (isFlipMode) Icons.Default.FlipToFront else Icons.Default.FlipToBack,
                    contentDescription = if (isFlipMode) "Show original quotes" else "Show flipped quotes",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = {
                isRotating = true
                playSound = true
                onRefreshClick()
            }) {
                LaunchedEffect(isRotating) {
                    if (isRotating) {
                        kotlinx.coroutines.delay(300)
                        isRotating = false
                    }
                }
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh quotes",
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
