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

    private val _quoteFont = mutableStateOf(getInitialQuoteFont())
    val quoteFont: State<String> = _quoteFont

    private val _authorFont = mutableStateOf(getInitialAuthorFont())
    val authorFont: State<String> = _authorFont

    private val _cardBackground = mutableStateOf(getInitialCardBackground())
    val cardBackground: State<String> = _cardBackground

    private fun getInitialTheme(): AppTheme {
        val savedTheme = sharedPreferences.getString("selected_theme", "black")
        return if (savedTheme == "black") AppThemes.BlackTheme else AppThemes.WhiteTheme
    }

    private fun getInitialQuoteFont(): String {
        return sharedPreferences.getString("quote_font", "kotta_one") ?: "kotta_one"
    }

    private fun getInitialAuthorFont(): String {
        return sharedPreferences.getString("author_font", "playfair_display") ?: "playfair_display"
    }

    private fun getInitialCardBackground(): String {
        return sharedPreferences.getString("card_background", "white") ?: "white"
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

    fun setCardBackground(colorName: String) {
        _cardBackground.value = colorName
        with(sharedPreferences.edit()) {
            putString("card_background", colorName)
            apply()
        }
    }

    fun isBlackTheme(): Boolean {
        return _currentTheme.value == AppThemes.BlackTheme
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

    fun getAvailableBackgrounds(): List<Pair<String, String>> {
        return listOf(
            "white" to "Classic Elegant",
            "gradient_sunset" to "Sunset Gradient",
            "minimalist" to "Modern Minimalist",
            "nature_pattern" to "Nature Organic"
        )
    }
}

// Composable to provide theme throughout the app
@Composable
fun rememberThemeManager(): ThemeManager {
    val context = LocalContext.current
    return remember { ThemeManager(context) }
}
