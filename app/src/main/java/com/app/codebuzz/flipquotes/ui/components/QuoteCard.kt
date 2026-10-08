@file:OptIn(ExperimentalFoundationApi::class)

package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.ui.theme.PlayfairDisplayFont
import com.app.codebuzz.flipquotes.ui.theme.QuoteFont
import com.app.codebuzz.flipquotes.ui.theme.fontFamilyFor
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun QuotePager(
    quotes: List<Quote>,
    pagerState: PagerState,
    themeManager: com.app.codebuzz.flipquotes.ui.theme.ThemeManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val nextQuoteLabel = stringResource(R.string.next_quote)
    val previousQuoteLabel = stringResource(R.string.previous_quote)

    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(), // Reverted to full height to ensure proper layout rendering across all screen sizes
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
                // Read quote and author as one item, with swipe alternatives for TalkBack
                .semantics(mergeDescendants = true) {
                    customActions = listOf(
                        CustomAccessibilityAction(nextQuoteLabel) {
                            coroutineScope.launch { pagerState.animateScrollToPage(page + 1) }
                            true
                        },
                        CustomAccessibilityAction(previousQuoteLabel) {
                            coroutineScope.launch { pagerState.animateScrollToPage(page - 1) }
                            true
                        }
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

                    // Main content - center the quote content, scrolling only when it doesn't fit
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        quotes.getOrNull(page)?.let { quote ->
                            QuoteContent(
                                quote = quote,
                                themeManager = themeManager,
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 64.dp)
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

    val quoteFontFamily = fontFamilyFor(currentQuoteFont, fallback = QuoteFont)
    val authorFontFamily = fontFamilyFor(currentAuthorFont, fallback = PlayfairDisplayFont)

    // Step the size down for long quotes so they stay on one screen
    val quoteStyle = when {
        quote.quote.length > 280 -> MaterialTheme.typography.titleLarge
        quote.quote.length > 180 -> MaterialTheme.typography.headlineSmall
        quote.quote.length > 100 -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineLarge
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text(
            text = "\"${quote.quote}\"",
            style = quoteStyle.copy(
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

// Bookmark and share for the quote on screen; drawn over the paper texture, so colors are fixed
@Composable
fun QuoteActions(
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val bookmarkScale = remember { Animatable(1f) }
    val buttonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = Color.Black.copy(alpha = 0.08f),
        contentColor = Color.Black
    )

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalIconButton(
            onClick = {
                haptic.performHapticFeedback(
                    if (isBookmarked) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn
                )
                if (!isBookmarked) {
                    coroutineScope.launch {
                        bookmarkScale.animateTo(1.35f, tween(120))
                        bookmarkScale.animateTo(1f, spring())
                    }
                }
                onBookmarkClick()
            },
            colors = buttonColors
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = stringResource(
                    if (isBookmarked) R.string.remove_bookmark else R.string.add_bookmark
                ),
                modifier = Modifier.graphicsLayer {
                    scaleX = bookmarkScale.value
                    scaleY = bookmarkScale.value
                }
            )
        }
        FilledTonalIconButton(onClick = onShareClick, colors = buttonColors) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share_quote)
            )
        }
    }
}

// One-time hint for new users; goes away after the first swipe or a tap
@Composable
fun SwipeHint(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bounce by rememberInfiniteTransition(label = "swipe-hint").animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipe-hint-bounce"
    )

    Surface(
        onClick = onDismiss,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.8f),
        contentColor = Color.White,
        modifier = modifier.graphicsLayer { translationY = bounce.dp.toPx() }
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.swipe_hint),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
