@file:OptIn(ExperimentalFoundationApi::class)

package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    isOnAllThemes: Boolean = true,
    header: @Composable () -> Unit = {}
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
                    // Texture background
                    Image(
                        painter = painterResource(id = R.drawable.texture),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Main content - center the quote content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragStart = {},
                                    onDragEnd = {},
                                    onHorizontalDrag = { _, dragAmount ->
                                        // Enhanced sensitivity - detect even the slightest swipes (reduced threshold from 50f to 20f)
                                        // Right to left swipe (negative dragAmount) - navigate to next theme
                                        if (dragAmount < -20f) {
                                            if (isOnAllThemes) {
                                                // On "All" theme: left swipe switches to next theme
                                                onThemeNext()
                                            } else {
                                                // On other themes: continue theme navigation
                                                onThemeNext()
                                            }
                                        }
                                        // Left to right swipe (positive dragAmount)
                                        else if (dragAmount > 20f) {
                                            if (isOnAllThemes) {
                                                // On "All" theme: right swipe opens menu
                                                onMenuOpen()
                                            } else {
                                                // On other themes: navigate to previous theme
                                                onThemePrevious()
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
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "~ ${quote.author}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = authorFontFamily
            ),
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ShareableQuoteCard(quote: Quote) {
    // Draw the texture background with rectangular corners, no black or solid color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Set black background for padding/edges
    ) {
        Card(
            shape = RectangleShape, // Rectangular corners
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.texture),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = quote.quote,
                            style = MaterialTheme.typography.headlineLarge
                            .copy(fontFamily = FontFamily(Font(resId = R.font.kotta_one))),
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "- ${quote.author}",
                            style = MaterialTheme.typography.bodyLarge
                            .copy(fontFamily = FontFamily(Font(resId = R.font.kotta_one))),
                            color = Color.DarkGray,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
