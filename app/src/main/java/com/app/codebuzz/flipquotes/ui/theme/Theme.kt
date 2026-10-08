package com.app.codebuzz.flipquotes.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.R

// Material colors are derived from the selected AppTheme so tabs, dialogs and sheets follow it
private fun AppTheme.toColorScheme(): ColorScheme = if (isDark) {
    darkColorScheme(
        primary = Color(0xFFE5E5E5),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF3A3A3A),
        onPrimaryContainer = Color.White,
        secondary = accentColor,
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF333333),
        onSecondaryContainer = Color.White,
        tertiary = Color(0xFFB0B0B0),
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFF3A3A3A),
        onTertiaryContainer = Color.White,
        background = backgroundColor,
        onBackground = onSurfaceColor,
        surface = surfaceColor,
        onSurface = onSurfaceColor,
        surfaceVariant = Color(0xFF2A2A2A),
        onSurfaceVariant = Color(0xFFB0B0B0),
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF121212),
        surfaceContainer = Color(0xFF1A1A1A),
        surfaceContainerHigh = Color(0xFF1F1F1F),
        surfaceContainerHighest = Color(0xFF2D2D2D),
        inverseSurface = Color(0xFF2D2D2D),
        inverseOnSurface = Color(0xFFE0E0E0),
        outline = Color(0xFF8A8A8A),
        outlineVariant = Color(0xFF3A3A3A)
    )
} else {
    lightColorScheme(
        primary = Color.Black,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0E0E0),
        onPrimaryContainer = Color.Black,
        secondary = accentColor,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE8E8E8),
        onSecondaryContainer = Color.Black,
        tertiary = Color(0xFF5F5F5F),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE0E0E0),
        onTertiaryContainer = Color.Black,
        background = backgroundColor,
        onBackground = onSurfaceColor,
        surface = surfaceColor,
        onSurface = onSurfaceColor,
        surfaceVariant = Color(0xFFEEEEEE),
        onSurfaceVariant = Color(0xFF4A4A4A),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFFAFAFA),
        surfaceContainer = Color(0xFFF5F5F5),
        surfaceContainerHigh = Color(0xFFF0F0F0),
        surfaceContainerHighest = Color(0xFFE6E6E6),
        inverseSurface = Color(0xFF2D2D2D),
        inverseOnSurface = Color.White,
        outline = Color(0xFF757575),
        outlineVariant = Color(0xFFD0D0D0)
    )
}

// Define custom font families
val QuoteFont = FontFamily(Font(R.font.kotta_one)) // Add lobster_regular.ttf to res/font
val AuthorFont = FontFamily(Font(R.font.kotta_one)) // Add roboto_italic.ttf to res/font
val PlayfairDisplayFont = FontFamily(Font(R.font.playfair_display))
val DroidSansFont = FontFamily(Font(R.font.droid_sans))

// Map font names to font resources (custom fonts and system fonts)
fun fontFamilyFor(fontKey: String, fallback: FontFamily): FontFamily = when (fontKey) {
    // Custom fonts from res/font
    "kotta_one" -> QuoteFont
    "playfair_display" -> PlayfairDisplayFont
    "droid_sans" -> DroidSansFont
    // Android system fonts
    "default" -> FontFamily.Default
    "sans_serif" -> FontFamily.SansSerif
    "serif" -> FontFamily.Serif
    "monospace" -> FontFamily.Monospace
    "cursive" -> FontFamily.Cursive
    "fantasy" -> FontFamily.SansSerif // Fantasy maps to SansSerif as fallback
    else -> fallback
}

val CustomTypography = Typography(
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = QuoteFont,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = AuthorFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

@Composable
fun FlipQuotesTheme(
    theme: AppTheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = theme.toColorScheme(),
        typography = CustomTypography, // Use custom typography
        shapes = Shapes(),
        content = content
    )
}
