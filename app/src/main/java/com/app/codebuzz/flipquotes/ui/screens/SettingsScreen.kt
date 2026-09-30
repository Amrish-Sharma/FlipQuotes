package com.app.codebuzz.flipquotes.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.data.ReminderPreferences
import com.app.codebuzz.flipquotes.notifications.NotificationHelper
import com.app.codebuzz.flipquotes.notifications.ReminderScheduler
import com.app.codebuzz.flipquotes.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val currentTheme by themeManager.currentTheme
    var showToast by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val reminderPrefs = remember { ReminderPreferences(context) }
    var reminderEnabled by remember { mutableStateOf(reminderPrefs.enabled) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show toast when settings change
    LaunchedEffect(showToast) {
        if (showToast) {
            snackbarHostState.showSnackbar(
                message = "Settings saved",
                duration = SnackbarDuration.Short
            )
            showToast = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(currentTheme.backgroundColor)
        ) {
            // Header for settings screen
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp, // Match MenuScreen
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.onPrimaryColor // Ensure color consistency
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = currentTheme.onPrimaryColor // Ensure color consistency
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = currentTheme.primaryColor,
                    titleContentColor = currentTheme.onPrimaryColor,
                    navigationIconContentColor = currentTheme.onPrimaryColor
                )
            )

            // Settings content with improved spacing and dividers
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Match MenuScreen spacing
            ) {
                item {
                    SettingsItemCard(
                        icon = Icons.Default.Palette,
                        title = "Appearance",
                        description = "Choose your preferred theme",
                        onClick = { showAppearanceDialog = true },
                        theme = currentTheme
                    )
                }

                // Visual divider between sections
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = currentTheme.onSurfaceColor.copy(alpha = 0.2f)
                    )
                }

                item {
                    SettingsItemCard(
                        icon = Icons.Default.TextFields,
                        title = "Font",
                        description = "Customize your quote and author fonts",
                        onClick = { showFontDialog = true },
                        theme = currentTheme
                    )
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = currentTheme.onSurfaceColor.copy(alpha = 0.2f)
                    )
                }

                item {
                    SettingsItemCard(
                        icon = Icons.Default.Notifications,
                        title = "Daily Reminder",
                        description = if (reminderEnabled) {
                            "On · %02d:%02d".format(reminderPrefs.hour, reminderPrefs.minute)
                        } else {
                            "Get the Quote of the Day as a notification"
                        },
                        onClick = { showReminderDialog = true },
                        theme = currentTheme
                    )
                }
            }
        }

        // Snackbar for save confirmation
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = if (currentTheme == com.app.codebuzz.flipquotes.ui.theme.AppThemes.BlackTheme) {
                    Color(0xFF2D2D2D) // Darker background for better contrast in black theme
                } else {
                    MaterialTheme.colorScheme.inverseSurface
                },
                contentColor = if (currentTheme == com.app.codebuzz.flipquotes.ui.theme.AppThemes.BlackTheme) {
                    Color(0xFFE0E0E0) // Lighter text for better contrast in black theme
                } else {
                    MaterialTheme.colorScheme.inverseOnSurface
                }
            )
        }

        // Appearance Dialog
        if (showAppearanceDialog) {
            AppearanceDialog(
                themeManager = themeManager,
                onDismiss = { showAppearanceDialog = false },
                onThemeChanged = {
                    showToast = true
                    showAppearanceDialog = false
                }
            )
        }

        // Font Dialog
        if (showFontDialog) {
            FontDialog(
                themeManager = themeManager,
                currentTheme = currentTheme,
                onDismiss = { showFontDialog = false },
                onFontChanged = {
                    showToast = true
                    showFontDialog = false
                }
            )
        }

        // Daily Reminder Dialog
        if (showReminderDialog) {
            ReminderDialog(
                reminderPrefs = reminderPrefs,
                isBlackTheme = themeManager.isBlackTheme(),
                onDismiss = { showReminderDialog = false },
                onSaved = {
                    reminderEnabled = reminderPrefs.enabled
                    showToast = true
                    showReminderDialog = false
                }
            )
        }
    }
}

@Composable
private fun ReminderDialog(
    reminderPrefs: ReminderPreferences,
    isBlackTheme: Boolean,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(reminderPrefs.enabled) }
    val timeState = rememberTimePickerState(
        initialHour = reminderPrefs.hour,
        initialMinute = reminderPrefs.minute,
        is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    )
    val textColor = if (isBlackTheme) Color(0xFFE5E5E5) else Color.Black
    val subtleColor = if (isBlackTheme) Color(0xFFB0B0B0) else Color.Black.copy(alpha = 0.7f)

    fun save() {
        reminderPrefs.enabled = enabled
        reminderPrefs.hour = timeState.hour
        reminderPrefs.minute = timeState.minute
        if (enabled) {
            ReminderScheduler.schedule(context, timeState.hour, timeState.minute)
        } else {
            ReminderScheduler.cancel(context)
        }
        onSaved()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            save()
        } else {
            enabled = false
            Toast.makeText(context, "Allow notifications to receive daily reminders", Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Daily Reminder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column {
                Text(
                    text = "Get the Quote of the Day as a notification",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtleColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remind me daily",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                if (enabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeInput(
                        state = timeState,
                        colors = if (isBlackTheme) {
                            TimePickerDefaults.colors(
                                timeSelectorSelectedContainerColor = Color(0xFFE5E5E5),
                                timeSelectorSelectedContentColor = Color.Black,
                                timeSelectorUnselectedContainerColor = Color(0xFF2D2D2D),
                                timeSelectorUnselectedContentColor = Color(0xFFE5E5E5),
                                periodSelectorSelectedContainerColor = Color(0xFFE5E5E5),
                                periodSelectorSelectedContentColor = Color.Black,
                                periodSelectorUnselectedContentColor = Color(0xFFB0B0B0)
                            )
                        } else {
                            TimePickerDefaults.colors()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val needsPermission = enabled && !NotificationHelper.canPostNotifications(context)
                    if (needsPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        save()
                    }
                }
            ) {
                Text("Save", color = if (isBlackTheme) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = subtleColor)
            }
        },
        containerColor = if (isBlackTheme) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AppearanceDialog(
    themeManager: ThemeManager,
    onDismiss: () -> Unit,
    onThemeChanged: () -> Unit
) {
    val isBlackTheme = themeManager.isBlackTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isBlackTheme) Color(0xFFE5E5E5) else Color.Black
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose your preferred theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isBlackTheme) Color(0xFFB0B0B0) else Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // White Theme Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = !isBlackTheme,
                            onClick = {
                                themeManager.setTheme("white")
                                onThemeChanged()
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !isBlackTheme,
                        onClick = {
                            themeManager.setTheme("white")
                            onThemeChanged()
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (isBlackTheme) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.primary,
                            unselectedColor = if (isBlackTheme) Color(0xFFB0B0B0) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "White theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isBlackTheme) Color(0xFFE5E5E5) else Color.Black
                    )
                }

                // Black Theme Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isBlackTheme,
                            onClick = {
                                themeManager.setTheme("black")
                                onThemeChanged()
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isBlackTheme,
                        onClick = {
                            themeManager.setTheme("black")
                            onThemeChanged()
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = if (isBlackTheme) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.primary,
                            unselectedColor = if (isBlackTheme) Color(0xFFB0B0B0) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Black theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isBlackTheme) Color(0xFFE5E5E5) else Color.Black
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    "Close",
                    color = if (isBlackTheme) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = if (isBlackTheme) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FontDialog(
    themeManager: ThemeManager,
    currentTheme: com.app.codebuzz.flipquotes.ui.theme.AppTheme,
    onDismiss: () -> Unit,
    onFontChanged: () -> Unit
) {
    val isBlackTheme = themeManager.isBlackTheme()
    val currentQuoteFont by themeManager.quoteFont
    val currentAuthorFont by themeManager.authorFont
    val availableFonts = themeManager.getAvailableFonts()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Fonts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isBlackTheme) Color(0xFFE5E5E5) else Color.Black
            )
        },
        text = {
            Column {
                Text(
                    text = "Customize your quote and author fonts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isBlackTheme) Color(0xFFB0B0B0) else Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Quote Font Section
                Text(
                    text = "Quote Font",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var quoteFontExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = quoteFontExpanded,
                    onExpandedChange = { quoteFontExpanded = !quoteFontExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = availableFonts.find { it.first == currentQuoteFont }?.second ?: "Kotta One",
                        onValueChange = {},
                        readOnly = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, fontSize = 18.sp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = quoteFontExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                            unfocusedTextColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                            focusedBorderColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                            unfocusedBorderColor = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.5f),
                            focusedTrailingIconColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                            unfocusedTrailingIconColor = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = quoteFontExpanded,
                        onDismissRequest = { quoteFontExpanded = false },
                        modifier = Modifier.background(if (isBlackTheme) Color(0xFF2A2A2A) else currentTheme.surfaceColor)
                    ) {
                        availableFonts.forEach { (fontKey, fontName) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = fontName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 18.sp,
                                        color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                                    )
                                },
                                onClick = {
                                    themeManager.setQuoteFont(fontKey)
                                    quoteFontExpanded = false
                                    onFontChanged()
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Author Font Section
                Text(
                    text = "Author Font",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var authorFontExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = authorFontExpanded,
                    onExpandedChange = { authorFontExpanded = !authorFontExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = availableFonts.find { it.first == currentAuthorFont }?.second ?: "Playfair Display",
                        onValueChange = {},
                        readOnly = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, fontSize = 18.sp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = authorFontExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                            unfocusedTextColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                            focusedBorderColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                            unfocusedBorderColor = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.5f),
                            focusedTrailingIconColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                            unfocusedTrailingIconColor = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = authorFontExpanded,
                        onDismissRequest = { authorFontExpanded = false },
                        modifier = Modifier.background(if (isBlackTheme) Color(0xFF2A2A2A) else currentTheme.surfaceColor)
                    ) {
                        availableFonts.forEach { (fontKey, fontName) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = fontName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 18.sp,
                                        color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                                    )
                                },
                                onClick = {
                                    themeManager.setAuthorFont(fontKey)
                                    authorFontExpanded = false
                                    onFontChanged()
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    "Close",
                    color = if (isBlackTheme) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.primary
                )
            }
        },
        containerColor = if (isBlackTheme) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    theme: com.app.codebuzz.flipquotes.ui.theme.AppTheme,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.surfaceColor.copy(alpha = 0.9f)
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
                tint = theme.onSurfaceColor,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = theme.onSurfaceColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = theme.onSurfaceColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
