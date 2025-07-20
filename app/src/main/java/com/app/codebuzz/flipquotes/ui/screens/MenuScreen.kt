package com.app.codebuzz.flipquotes.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White) // Set blank/white background
    ) {
        // Header for menu screen
        TopAppBar(
            title = {
                Text(
                    text = "Menu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        // Menu content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                MenuItemCard(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    description = "App preferences and configuration",
                    onClick = { /* TODO: Navigate to settings */ }
                )
            }

            item {
                MenuItemCard(
                    icon = Icons.Default.Star,
                    title = "Rate App",
                    description = "Rate FlipQuotes on Play Store",
                    onClick = {
                        openPlayStore(context)
                    }
                )
            }

            item {
                MenuItemCard(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    description = "Share FlipQuotes with friends",
                    onClick = {
                        shareApp(context)
                    }
                )
            }

            item {
                MenuItemCard(
                    icon = Icons.Default.Info,
                    title = "About",
                    description = "App version and information",
                    onClick = { showDialog = true }
                )
            }
        }
    }

    // About dialog
    if (showDialog) {
        AboutDialog(onDismiss = { showDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun openPlayStore(context: Context) {
    val playStoreUrl = "https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes&hl=en&utm_source=flipQuoteApp&utm_medium=app&utm_campaign=rateApp"

    try {
        val intent = Intent(Intent.ACTION_VIEW, playStoreUrl.toUri())
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle case where no browser is available or other errors
        e.printStackTrace()
    }
}

private fun shareApp(context: Context) {
    val shareText = """
        🌟 Discover FlipQuotes - Your Daily Dose of Inspiration! 🌟
        
        Get motivated with beautiful, inspiring quotes that flip your perspective every day! ✨
        
        📱 Features:
        • Thousands of inspiring quotes
        • Beautiful themes & designs
        • Easy sharing with friends
        • Bookmark your favorites
        
        Download now and start your journey to daily inspiration:
        https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes&utm_source=share&utm_medium=app
        
        #FlipQuotes #Inspiration #Motivation #Quotes
    """.trimIndent()

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "Share FlipQuotes"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val packageName = context.packageName
    var version: String
    var versionCode: Long

    try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        version = packageInfo.versionName ?: "N/A"
        @Suppress("DEPRECATION")
        versionCode =
            packageInfo.longVersionCode
    } catch (_: PackageManager.NameNotFoundException) {
        version = "N/A"
        versionCode = -1
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "About FlipQuotes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "FlipQuotes is your daily source of inspiration, providing thousands of carefully curated quotes to motivate and uplift you.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version Information
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$version ($versionCode)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Developer: Code Ovanta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
