@file:OptIn(ExperimentalFoundationApi::class)

package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    isRefreshing: Boolean = false,
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
        modifier = Modifier.fillMaxSize(),
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
                shape = RoundedCornerShape(20.dp),
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                        ) {
                            header()
                        }

                        // Main content
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
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
                                QuoteContent(quote = quote)
                            }
                        }
                        // Footer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .navigationBarsPadding()
                        ) {
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
