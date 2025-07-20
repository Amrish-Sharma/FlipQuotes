package com.app.codebuzz.flipquotes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import com.app.codebuzz.flipquotes.ui.theme.ThemeManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val currentTheme by themeManager.currentTheme
    var showToast by remember { mutableStateOf(false) }
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = currentTheme.onPrimaryColor
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
                verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing between sections
            ) {
                item {
                    ThemeSelectionSection(
                        themeManager = themeManager,
                        currentTheme = currentTheme,
                        onSettingChanged = { showToast = true }
                    )
                }

                // Visual divider between sections
                item {
                    Divider(
                        color = currentTheme.onSurfaceColor.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    FontSelectionSection(
                        themeManager = themeManager,
                        currentTheme = currentTheme,
                        onSettingChanged = { showToast = true }
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
    }
}

@Composable
private fun ThemeSelectionSection(
    themeManager: ThemeManager,
    currentTheme: com.app.codebuzz.flipquotes.ui.theme.AppTheme,
    onSettingChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isBlackTheme = themeManager.isBlackTheme()
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlackTheme) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Clickable Header with icon and expand/collapse arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(bottom = if (isExpanded) 16.dp else 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Theme",
                    tint = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Animated expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column {
                    Text(
                        text = "Choose your preferred theme",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.7f),
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
                                    onSettingChanged()
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
                                onSettingChanged()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                                unselectedColor = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.6f)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "White Theme",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal,
                                color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                            )
//                            Text(
//                                text = "Light background with dark text",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.7f)
//                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Black Theme Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isBlackTheme,
                                onClick = {
                                    themeManager.setTheme("black")
                                    onSettingChanged()
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
                                onSettingChanged()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                                unselectedColor = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.6f)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Black Theme",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal,
                                color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                            )
//                            Text(
//                                text = "Dark background with light text",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.7f)
//                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontSelectionSection(
    themeManager: ThemeManager,
    currentTheme: com.app.codebuzz.flipquotes.ui.theme.AppTheme,
    onSettingChanged: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentQuoteFont by themeManager.quoteFont
    val currentAuthorFont by themeManager.authorFont
    val isBlackTheme = themeManager.isBlackTheme()
    var isExpanded by remember { mutableStateOf(false) }

    // Get available fonts from ThemeManager (includes both custom and system fonts)
    val availableFonts = themeManager.getAvailableFonts()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlackTheme) Color(0xFF1A1A1A) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Clickable Header with icon and expand/collapse arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(bottom = if (isExpanded) 16.dp else 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = "Font",
                    tint = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Font",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Animated expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column {
//                    Text(
//                        text = "Choose your preferred fonts (includes system fonts)",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = if (isBlackTheme) Color(0xFFB0B0B0) else currentTheme.onSurfaceColor.copy(alpha = 0.7f),
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )

                    // Quote Font Section
                    Text(
                        text = "Quote Font",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quoteFontExpanded) },
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
                                            color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                                        )
                                    },
                                    onClick = {
                                        themeManager.setQuoteFont(fontKey)
                                        quoteFontExpanded = false
                                        onSettingChanged()
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
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = authorFontExpanded) },
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
                                            color = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                                        )
                                    },
                                    onClick = {
                                        themeManager.setAuthorFont(fontKey)
                                        authorFontExpanded = false
                                        onSettingChanged()
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = if (isBlackTheme) Color(0xFFE5E5E5) else currentTheme.onSurfaceColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
