package com.app.codebuzz.flipquotes.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Theme data class to hold color scheme
data class AppTheme(
    val backgroundColor: Color,
    val surfaceColor: Color,
    val onSurfaceColor: Color,
    val primaryColor: Color,
    val onPrimaryColor: Color
)

// Define theme variants
object AppThemes {
    val WhiteTheme = AppTheme(
        backgroundColor = Color.White,
        surfaceColor = Color.White,
        onSurfaceColor = Color.Black,
        primaryColor = Color.White,
        onPrimaryColor = Color.Black
    )

    val BlackTheme = AppTheme(
        backgroundColor = Color.Black,
        surfaceColor = Color.Black,
        onSurfaceColor = Color.White,
        primaryColor = Color.Black,
        onPrimaryColor = Color.White
    )
}

// Theme manager class
class ThemeManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

    private val _currentTheme = mutableStateOf(getInitialTheme())
    val currentTheme: State<AppTheme> = _currentTheme

    private fun getInitialTheme(): AppTheme {
        val savedTheme = sharedPreferences.getString("selected_theme", "black")
        return if (savedTheme == "black") AppThemes.BlackTheme else AppThemes.WhiteTheme
    }

    fun setTheme(themeName: String) {
        val newTheme = if (themeName == "black") AppThemes.BlackTheme else AppThemes.WhiteTheme
        _currentTheme.value = newTheme

        // Save to SharedPreferences
        with(sharedPreferences.edit()) {
            putString("selected_theme", themeName)
            apply()
        }
    }

    fun isBlackTheme(): Boolean {
        return _currentTheme.value == AppThemes.BlackTheme
    }
}

// Composable to provide theme throughout the app
@Composable
fun rememberThemeManager(): ThemeManager {
    val context = LocalContext.current
    return remember { ThemeManager(context) }
}
