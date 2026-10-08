package com.app.codebuzz.flipquotes.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.ui.theme.AppTheme
import com.app.codebuzz.flipquotes.ui.theme.PlayfairDisplayFont

// Navigation drawer content
@Composable
fun MenuDrawerContent(
    onSettingsClick: () -> Unit,
    onDailyQuoteClick: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = theme.backgroundColor,
        drawerContentColor = theme.onSurfaceColor
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PlayfairDisplayFont,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )

            MenuListItem(
                icon = Icons.Default.WbSunny,
                title = stringResource(R.string.quote_of_the_day),
                description = stringResource(R.string.quote_of_the_day_description),
                onClick = onDailyQuoteClick,
                theme = theme
            )
            MenuListItem(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.settings),
                description = stringResource(R.string.settings_description),
                onClick = onSettingsClick,
                theme = theme
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                thickness = 1.dp,
                color = theme.onSurfaceColor.copy(alpha = 0.2f)
            )

            MenuListItem(
                icon = Icons.Default.Star,
                title = stringResource(R.string.rate_app),
                description = stringResource(R.string.rate_app_description),
                onClick = {
                    openPlayStore(context)
                },
                theme = theme
            )
            MenuListItem(
                icon = Icons.Default.Share,
                title = stringResource(R.string.share_app),
                description = stringResource(R.string.share_app_description),
                onClick = {
                    shareApp(context)
                },
                theme = theme
            )
            MenuListItem(
                icon = Icons.Default.Info,
                title = stringResource(R.string.about),
                description = stringResource(R.string.about_description),
                onClick = { showDialog = true },
                theme = theme
            )
        }
    }

    // About dialog
    if (showDialog) {
        AboutDialog(onDismiss = { showDialog = false })
    }
}

// Plain list row shared by the menu and the settings screen
@Composable
internal fun MenuListItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = theme.onSurfaceColor,
            supportingColor = theme.onSurfaceColor.copy(alpha = 0.7f),
            leadingIconColor = theme.onSurfaceColor
        ),
        modifier = modifier.clickable(onClick = onClick)
    )
}

private fun openPlayStore(context: Context) {
    val playStoreUrl = "https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes&hl=en&utm_source=flipQuoteApp&utm_medium=app&utm_campaign=rateApp"

    try {
        val intent = Intent(Intent.ACTION_VIEW, playStoreUrl.toUri())
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle case where no browser is available or other errors
        Toast.makeText(context, context.getString(R.string.play_store_error), Toast.LENGTH_LONG).show()
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
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_app_chooser)))
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
                    text = stringResource(R.string.about_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.about_body),
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
                        text = stringResource(R.string.about_developer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
