package com.app.codebuzz.flipquotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.R

private val DarkColorScheme = darkColorScheme(
    primary = Color.Black,
    secondary = Color.White,
    tertiary = Color.Gray,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = Color.White,
    tertiary = Color.Gray,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

// Define custom font families
val QuoteFont = FontFamily(Font(R.font.kotta_one)) // Add lobster_regular.ttf to res/font
val AuthorFont = FontFamily(Font(R.font.kotta_one)) // Add roboto_italic.ttf to res/font

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CustomTypography, // Use custom typography
        shapes = Shapes(),
        content = content
    )
}
