package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.codebuzz.flipquotes.data.Quote
import kotlinx.coroutines.launch
import com.app.codebuzz.flipquotes.R

@Composable
fun QuoteCard(
    quote: Quote,
    swipeDirection: Int = 0, // 1 for next, -1 for previous
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    likeCount: String = "0",
    isLiked: Boolean = false,
    isBookmarked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    var isAnimating by remember { mutableStateOf(false) }

    // Animate card entry based on swipeDirection
    LaunchedEffect(swipeDirection, quote) {
        if (swipeDirection == 1) {
            // Coming from bottom
            offsetY.snapTo(1000f)
            alpha.snapTo(0f)
            offsetY.animateTo(0f, animationSpec = tween(300))
            alpha.animateTo(1f, animationSpec = tween(200))
        } else if (swipeDirection == -1) {
            // Coming from top
            offsetY.snapTo(-1000f)
            alpha.snapTo(0f)
            offsetY.animateTo(0f, animationSpec = tween(300))
            alpha.animateTo(1f, animationSpec = tween(200))
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxSize()
            //.padding(16.dp)
            .graphicsLayer {
                translationY = offsetY.value
                this.alpha = alpha.value
            }
            .pointerInput(isAnimating) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        when {
                            offsetY.value < -100f && !isAnimating -> {
                                isAnimating = true
                                scope.launch {
                                    try {
                                        // Animate out (up)
                                        offsetY.animateTo(-1000f, animationSpec = tween(300))
                                        alpha.animateTo(0f, animationSpec = tween(200))
                                        // Trigger next quote
                                        onNext()
                                        // Wait for recomposition with new quote
                                        // Snap to bottom, invisible
                                        offsetY.snapTo(1000f)
                                        alpha.snapTo(0f)
                                        // Animate in (from bottom)
                                        offsetY.animateTo(0f, animationSpec = tween(300))
                                        alpha.animateTo(1f, animationSpec = tween(200))
                                    } finally {
                                        isAnimating = false
                                    }
                                }
                            }
                            offsetY.value > 100f && !isAnimating -> {
                                isAnimating = true
                                scope.launch {
                                    try {
                                        // Animate out (down)
                                        offsetY.animateTo(1000f, animationSpec = tween(300))
                                        alpha.animateTo(0f, animationSpec = tween(200))
                                        // Trigger previous quote
                                        onPrevious()
                                        // Wait for recomposition with new quote
                                        // Snap to top, invisible
                                        offsetY.snapTo(-1000f)
                                        alpha.snapTo(0f)
                                        // Animate in (from top)
                                        offsetY.animateTo(0f, animationSpec = tween(300))
                                        alpha.animateTo(1f, animationSpec = tween(200))
                                    } finally {
                                        isAnimating = false
                                    }
                                }
                            }
                            else -> {
                                scope.launch {
                                    offsetY.animateTo(0f, animationSpec = tween(300))
                                }
                            }
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (!isAnimating) {
                            scope.launch {
                                val newOffset = (offsetY.value + dragAmount).coerceIn(-1200f, 1200f)
                                offsetY.snapTo(newOffset)
                            }
                        }
                    }
                )
            },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Texture background covers the entire card
            Image(
                painter = painterResource(id = R.drawable.texture),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with status bar padding
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    header()
                }
                // Main content (no system bar padding)
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    QuoteContent(quote = quote)
                }
                // Footer with navigation bar padding and matching background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface) // Match footer bg
                        .navigationBarsPadding()
                ) {
                    footer()
                    Footer(
                        likeCount = likeCount,
                        isLiked = isLiked,
                        isBookmarked = isBookmarked,
                        onLikeClick = onLikeClick,
                        onShareClick = onShareClick,
                        onBookmarkClick = onBookmarkClick
                    )
                }
            }
        }
    }
}

@Composable
fun QuoteContent(quote: Quote, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text(
            text = "\"${quote.quote}\"",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily(Font(resId = R.font.kotta_one))
            ),
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "~ ${quote.author}",
            style = MaterialTheme.typography.bodyLarge,
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
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
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
