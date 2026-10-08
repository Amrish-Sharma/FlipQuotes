package com.app.codebuzz.flipquotes.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Theme data class to hold color scheme
data class AppTheme(
    val backgroundColor: Color,
    val surfaceColor: Color,
    val onSurfaceColor: Color,
    val primaryColor: Color,
    val onPrimaryColor: Color,
    val accentColor: Color,
    val isDark: Boolean
)

// Define theme variants
object AppThemes {
    val WhiteTheme = AppTheme(
        backgroundColor = Color.White,
        surfaceColor = Color.White,
        onSurfaceColor = Color.Black,
        primaryColor = Color.White,
        onPrimaryColor = Color.Black,
        accentColor = Color(0xFF8A5A00),
        isDark = false
    )

    val BlackTheme = AppTheme(
        backgroundColor = Color.Black,
        surfaceColor = Color.Black,
        onSurfaceColor = Color.White,
        primaryColor = Color.Black,
        onPrimaryColor = Color.White,
        accentColor = Color(0xFFFFD54F),
        isDark = true
    )
}

// Theme manager class
class ThemeManager(context: Context, private val systemInDarkTheme: State<Boolean>) {

    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_WHITE = "white"
        const val THEME_BLACK = "black"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)

    private val _themeMode = mutableStateOf(getInitialThemeMode())
    val themeMode: State<String> = _themeMode

    val currentTheme: State<AppTheme> = derivedStateOf {
        when (_themeMode.value) {
            THEME_BLACK -> AppThemes.BlackTheme
            THEME_WHITE -> AppThemes.WhiteTheme
            else -> if (systemInDarkTheme.value) AppThemes.BlackTheme else AppThemes.WhiteTheme
        }
    }

    private val _quoteFont = mutableStateOf(getInitialQuoteFont())
    val quoteFont: State<String> = _quoteFont

    private val _authorFont = mutableStateOf(getInitialAuthorFont())
    val authorFont: State<String> = _authorFont

    private val _swipeHintSeen = mutableStateOf(sharedPreferences.getBoolean("swipe_hint_seen", false))
    val swipeHintSeen: State<Boolean> = _swipeHintSeen

    private fun getInitialThemeMode(): String {
        return sharedPreferences.getString("selected_theme", THEME_SYSTEM) ?: THEME_SYSTEM
    }

    private fun getInitialQuoteFont(): String {
        return sharedPreferences.getString("quote_font", "kotta_one") ?: "kotta_one"
    }

    private fun getInitialAuthorFont(): String {
        return sharedPreferences.getString("author_font", "playfair_display") ?: "playfair_display"
    }

    fun setTheme(themeName: String) {
        _themeMode.value = themeName

        // Save to SharedPreferences
        with(sharedPreferences.edit()) {
            putString("selected_theme", themeName)
            apply()
        }
    }

    fun setQuoteFont(fontName: String) {
        _quoteFont.value = fontName
        with(sharedPreferences.edit()) {
            putString("quote_font", fontName)
            apply()
        }
    }

    fun setAuthorFont(fontName: String) {
        _authorFont.value = fontName
        with(sharedPreferences.edit()) {
            putString("author_font", fontName)
            apply()
        }
    }

    fun markSwipeHintSeen() {
        if (_swipeHintSeen.value) return
        _swipeHintSeen.value = true
        with(sharedPreferences.edit()) {
            putBoolean("swipe_hint_seen", true)
            apply()
        }
    }

    fun isBlackTheme(): Boolean {
        return currentTheme.value.isDark
    }

    fun getAvailableFonts(): List<Pair<String, String>> {
        return listOf(
            // Custom fonts from res/font
            "kotta_one" to "Kotta One",
            "playfair_display" to "Playfair Display",
            "droid_sans" to "Droid Sans",
            // Android system fonts
            "default" to "Default",
            "sans_serif" to "Sans Serif",
            "serif" to "Serif",
            "monospace" to "Monospace",
            "cursive" to "Cursive",
            "fantasy" to "Fantasy"
        )
    }
}

// Composable to provide theme throughout the app
@Composable
fun rememberThemeManager(): ThemeManager {
    val context = LocalContext.current
    val systemInDarkTheme = rememberUpdatedState(isSystemInDarkTheme())
    return remember { ThemeManager(context, systemInDarkTheme) }
}
