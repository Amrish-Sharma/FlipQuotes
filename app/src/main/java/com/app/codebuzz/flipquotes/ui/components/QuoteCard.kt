@file:OptIn(ExperimentalFoundationApi::class)

package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.data.Quote
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun QuoteCard(
    quote: Quote,
    themeManager: com.app.codebuzz.flipquotes.ui.theme.ThemeManager,
    isRefreshing: Boolean = false,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onMenuOpen: () -> Unit = {},
    onThemeNext: () -> Unit = {},
    onThemePrevious: () -> Unit = {},
    isOnAllThemes: Boolean = true
) {
    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2,
        pageCount = { Int.MAX_VALUE }
    )

    var lastPage by remember { mutableIntStateOf(pagerState.currentPage) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage ->
            if (currentPage > lastPage) {
                onNext()
            } else if (currentPage < lastPage) {
                onPrevious()
            }
            lastPage = currentPage
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(), // Reverted to full height to ensure proper layout rendering across all screen sizes
        pageSpacing = 8.dp
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val pageOffset = ((pagerState.currentPage - page) + pagerState
                        .currentPageOffsetFraction).absoluteValue

                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )

                    translationY = lerp(
                        start = 0f,
                        stop = -200f,
                        fraction = pageOffset.coerceIn(0f, 1f)
                    )
                }
        ) {
            Card(
                shape = RectangleShape,
                modifier = Modifier.fillMaxSize(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Dynamic background based on user preference
                    CardBackground(
                        backgroundType = themeManager.cardBackground.value,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Main content - center the quote content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(isOnAllThemes) {
                                detectHorizontalDragGestures(
                                    onDragStart = {},
                                    onDragEnd = {},
                                    onHorizontalDrag = { _, dragAmount ->
                                        // Enhanced sensitivity - detect even the slightest swipes (reduced threshold from 20f to 10f)
                                        // Left to right swipe (positive dragAmount)
                                        if (dragAmount > 10f) {
                                            if (isOnAllThemes) {
                                                // On "All" theme: right swipe opens menu
                                                onMenuOpen()
                                            } else {
                                                // On other themes: navigate to previous theme
                                                onThemePrevious()
                                            }
                                        }
                                        // Right to left swipe (negative dragAmount) - navigate to next theme
                                        else if (dragAmount < -10f) {
                                            if (isOnAllThemes) {
                                                // On "All" theme: left swipe switches to next theme
                                                onThemeNext()
                                            } else {
                                                // On other themes: continue theme navigation
                                                onThemeNext()
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshing) {
                            // Use AnimatedContent only during refresh
                            AnimatedContent(
                                targetState = quote,
                                transitionSpec = {
                                    ContentTransform(
                                        targetContentEnter = slideInVertically { height -> height } +
                                            fadeIn(animationSpec = tween(300)),
                                        initialContentExit = slideOutVertically { height -> -height } +
                                            fadeOut(animationSpec = tween(300)),
                                        sizeTransform = null
                                    )
                                },
                                label = "quote transition"
                            ) { currentQuote ->
                                QuoteContent(quote = currentQuote)
                            }
                        } else {
                            // Direct content update during scrolling
                            QuoteContent(
                                quote = quote,
                                themeManager = themeManager
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteContent(
    modifier: Modifier = Modifier,
    quote: Quote,
    themeManager: com.app.codebuzz.flipquotes.ui.theme.ThemeManager? = null
) {
    val currentQuoteFont by remember { themeManager?.quoteFont ?: mutableStateOf("kotta_one") }
    val currentAuthorFont by remember { themeManager?.authorFont ?: mutableStateOf("playfair_display") }
    val currentBackground by remember { themeManager?.cardBackground ?: mutableStateOf("white") }

    // Determine text colors based on background
    val (textColor, authorColor) = when (currentBackground) {
        "gradient_sunset" -> Color.White to Color(0xFFE0E0E0)
        "minimalist" -> Color(0xFF2C3E50) to Color(0xFF5D6D7E)
        "nature_pattern" -> Color(0xFF2E7D32) to Color(0xFF4CAF50)
        else -> Color.Black to Color.DarkGray // white background and fallback
    }

    // Map font names to font resources (custom fonts and system fonts)
    val quoteFontFamily = when (currentQuoteFont) {
        // Custom fonts from res/font
        "kotta_one" -> FontFamily(Font(R.font.kotta_one))
        "playfair_display" -> FontFamily(Font(R.font.playfair_display))
        "droid_sans" -> FontFamily(Font(R.font.droid_sans))
        // Android system fonts
        "default" -> FontFamily.Default
        "sans_serif" -> FontFamily.SansSerif
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        "fantasy" -> FontFamily.SansSerif // Fantasy maps to SansSerif as fallback
        else -> FontFamily(Font(R.font.kotta_one))
    }

    val authorFontFamily = when (currentAuthorFont) {
        // Custom fonts from res/font
        "kotta_one" -> FontFamily(Font(R.font.kotta_one))
        "playfair_display" -> FontFamily(Font(R.font.playfair_display))
        "droid_sans" -> FontFamily(Font(R.font.droid_sans))
        // Android system fonts
        "default" -> FontFamily.Default
        "sans_serif" -> FontFamily.SansSerif
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        "fantasy" -> FontFamily.SansSerif // Fantasy maps to SansSerif as fallback
        else -> FontFamily(Font(R.font.playfair_display))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text(
            text = "\"${quote.quote}\"",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = quoteFontFamily
            ),
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "~ ${quote.author}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = authorFontFamily
            ),
            color = authorColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
