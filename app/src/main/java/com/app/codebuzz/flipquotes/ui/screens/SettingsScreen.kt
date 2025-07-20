package com.app.codebuzz.flipquotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val currentTheme by themeManager.currentTheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.backgroundColor)
    ) {
        // Header for settings screen
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
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

        // Settings content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ThemeSelectionSection(
                    themeManager = themeManager,
                    currentTheme = currentTheme
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionSection(
    themeManager: ThemeManager,
    currentTheme: com.app.codebuzz.flipquotes.ui.theme.AppTheme,
    modifier: Modifier = Modifier
) {
    val isBlackTheme = themeManager.isBlackTheme()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlackTheme) Color.Gray.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = currentTheme.onSurfaceColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Choose your preferred theme",
                style = MaterialTheme.typography.bodyMedium,
                color = currentTheme.onSurfaceColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // White Theme Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = !isBlackTheme,
                        onClick = { themeManager.setTheme("white") },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isBlackTheme,
                    onClick = { themeManager.setTheme("white") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = currentTheme.onSurfaceColor,
                        unselectedColor = currentTheme.onSurfaceColor.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "White Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = currentTheme.onSurfaceColor
                    )
                    Text(
                        text = "Light background with dark text",
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.onSurfaceColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Black Theme Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isBlackTheme,
                        onClick = { themeManager.setTheme("black") },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isBlackTheme,
                    onClick = { themeManager.setTheme("black") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = currentTheme.onSurfaceColor,
                        unselectedColor = currentTheme.onSurfaceColor.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Black Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = currentTheme.onSurfaceColor
                    )
                    Text(
                        text = "Dark background with light text",
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.onSurfaceColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
