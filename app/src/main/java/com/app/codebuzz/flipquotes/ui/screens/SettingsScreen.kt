package com.app.codebuzz.flipquotes.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.R
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
    val settingsSavedMessage = stringResource(R.string.settings_saved)

    BackHandler(onBack = onBackClick)

    // Show toast when settings change
    LaunchedEffect(showToast) {
        if (showToast) {
            snackbarHostState.showSnackbar(
                message = settingsSavedMessage,
                duration = SnackbarDuration.Short
            )
            showToast = false
        }
    }

    // Surface also keeps touches from reaching the screens underneath
    Surface(color = currentTheme.backgroundColor, modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header for settings screen
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings),
                            fontSize = 20.sp, // Match MenuScreen
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.onPrimaryColor // Ensure color consistency
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        MenuListItem(
                            icon = Icons.Default.Palette,
                            title = stringResource(R.string.appearance),
                            description = stringResource(R.string.appearance_description),
                            onClick = { showAppearanceDialog = true },
                            theme = currentTheme
                        )
                    }

                    item {
                        MenuListItem(
                            icon = Icons.Default.TextFields,
                            title = stringResource(R.string.font),
                            description = stringResource(R.string.font_description),
                            onClick = { showFontDialog = true },
                            theme = currentTheme
                        )
                    }

                    item {
                        MenuListItem(
                            icon = Icons.Default.Notifications,
                            title = stringResource(R.string.daily_reminder),
                            description = if (reminderEnabled) {
                                stringResource(
                                    R.string.daily_reminder_on,
                                    "%02d:%02d".format(reminderPrefs.hour, reminderPrefs.minute)
                                )
                            } else {
                                stringResource(R.string.daily_reminder_description)
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
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
            onDismiss = { showReminderDialog = false },
            onSaved = {
                reminderEnabled = reminderPrefs.enabled
                showToast = true
                showReminderDialog = false
            }
        )
    }
}

@Composable
private fun ReminderDialog(
    reminderPrefs: ReminderPreferences,
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
    val permissionNeededMessage = stringResource(R.string.notification_permission_needed)

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
            Toast.makeText(context, permissionNeededMessage, Toast.LENGTH_LONG).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.daily_reminder),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.daily_reminder_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.remind_me_daily),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                if (enabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeInput(state = timeState)
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AppearanceDialog(
    themeManager: ThemeManager,
    onDismiss: () -> Unit,
    onThemeChanged: () -> Unit
) {
    val themeMode by themeManager.themeMode
    val options = listOf(
        ThemeManager.THEME_SYSTEM to R.string.theme_system,
        ThemeManager.THEME_WHITE to R.string.theme_white,
        ThemeManager.THEME_BLACK to R.string.theme_black
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.appearance),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.appearance_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                options.forEach { (mode, label) ->
                    val selectTheme = {
                        themeManager.setTheme(mode)
                        onThemeChanged()
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = themeMode == mode,
                                onClick = selectTheme,
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode == mode,
                            onClick = selectTheme
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.close))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FontDialog(
    themeManager: ThemeManager,
    onDismiss: () -> Unit,
    onFontChanged: () -> Unit
) {
    val currentQuoteFont by themeManager.quoteFont
    val currentAuthorFont by themeManager.authorFont
    val availableFonts = themeManager.getAvailableFonts()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_fonts),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.font_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                FontDropdown(
                    label = stringResource(R.string.quote_font),
                    selectedFontName = availableFonts.find { it.first == currentQuoteFont }?.second ?: "Kotta One",
                    availableFonts = availableFonts,
                    onFontSelected = { fontKey ->
                        themeManager.setQuoteFont(fontKey)
                        onFontChanged()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FontDropdown(
                    label = stringResource(R.string.author_font),
                    selectedFontName = availableFonts.find { it.first == currentAuthorFont }?.second ?: "Playfair Display",
                    availableFonts = availableFonts,
                    onFontSelected = { fontKey ->
                        themeManager.setAuthorFont(fontKey)
                        onFontChanged()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.close))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontDropdown(
    label: String,
    selectedFontName: String,
    availableFonts: List<Pair<String, String>>,
    onFontSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedFontName,
            onValueChange = {},
            readOnly = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableFonts.forEach { (fontKey, fontName) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = fontName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    },
                    onClick = {
                        expanded = false
                        onFontSelected(fontKey)
                    }
                )
            }
        }
    }
}
