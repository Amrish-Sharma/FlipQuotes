package com.app.codebuzz.flipquotes.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.MediaStore.Images.Media
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.ui.components.QuoteCard
import com.app.codebuzz.flipquotes.ui.components.Header
import com.app.codebuzz.flipquotes.ui.components.QuoteFooter
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun QuotePagerScreen(viewModel: QuotesViewModel) {
    val quotes by viewModel.filteredQuotes.collectAsStateWithLifecycle(initialValue = emptyList())
    val themesList by viewModel.themesList.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle(initialValue = null)

    var currentQuoteIndex by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val likeStates = remember { mutableStateMapOf<Int, Boolean>() }
    val bookmarkStates = remember { mutableStateMapOf<Int, Boolean>() }
    val likeCounts = remember { mutableStateMapOf<Int, Int>() }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { themesList.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Smooth entrance animation state
    var isAppVisible by remember { mutableStateOf(false) }

    // Trigger smooth fade-in after a short delay to ensure everything is positioned
    LaunchedEffect(quotes, themesList) {
        if (quotes.isNotEmpty() && themesList.isNotEmpty() && !isAppVisible) {
            kotlinx.coroutines.delay(100) // Small delay for smooth positioning
            isAppVisible = true
        }
    }

    // Optimize tab switching with immediate state updates
    LaunchedEffect(pagerState.currentPage) {
        val newTheme = themesList.getOrNull(pagerState.currentPage)
        if (newTheme != selectedTheme) {
            viewModel.setSelectedTheme(newTheme)
        }
    }

    // Reset quote index when theme changes and ensure it's always within bounds
    LaunchedEffect(selectedTheme, quotes.size) {
        currentQuoteIndex = 0
    }

    // Ensure currentQuoteIndex is always within bounds
    val safeQuoteIndex = if (quotes.isNotEmpty()) {
        currentQuoteIndex.coerceIn(0, quotes.size - 1)
    } else 0

    // Smooth fade-in animation for the entire app content
    AnimatedVisibility(
        visible = isAppVisible,
        enter = fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    animationSpec = tween(600, easing = FastOutSlowInEasing),
                    initialOffsetY = { -50 }
                )
    ) {
        Scaffold(
            topBar = {
                Column {
                    Header(
                        onRefreshClick = {
                            isRefreshing = true
                            viewModel.forceRefresh()
                            isRefreshing = false
                            currentQuoteIndex = 0
                        }
                    )

                    if (themesList.isNotEmpty()) {
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            edgePadding = 12.dp,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            indicator = { tabPositions ->
                                if (tabPositions.isNotEmpty() && pagerState.currentPage < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        ) {
                            themesList.forEachIndexed { index, theme ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(index)
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = theme,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (quotes.isNotEmpty()) {
                    QuoteFooter(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        likeCount = (likeCounts[safeQuoteIndex] ?: 0).toString(),
                        isLiked = likeStates[safeQuoteIndex] == true,
                        isBookmarked = bookmarkStates[safeQuoteIndex] == true,
                        onLikeClick = {
                            likeStates[safeQuoteIndex] = likeStates[safeQuoteIndex] != true
                            likeCounts[safeQuoteIndex] = (likeCounts[safeQuoteIndex] ?: 0) +
                                    if (likeStates[safeQuoteIndex] == true) 1 else -1
                        },
                        onShareClick = {
                            shareQuoteImage(context, quotes[safeQuoteIndex])
                        },
                        onBookmarkClick = {
                            bookmarkStates[safeQuoteIndex] = bookmarkStates[safeQuoteIndex] != true
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (quotes.isEmpty()) {
                    Text(
                        text = "No quotes available for this theme",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            QuoteCard(
                                quote = quotes[safeQuoteIndex],
                                isRefreshing = isRefreshing,
                                onNext = {
                                    if (quotes.isNotEmpty()) {
                                        currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
                                    }
                                },
                                onPrevious = {
                                    if (quotes.isNotEmpty()) {
                                        currentQuoteIndex = (currentQuoteIndex - 1 + quotes.size) % quotes.size
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun shareQuoteImage(context: Context, quote: Quote) {
    val activity = context as? Activity ?: return
    val composeView = ComposeView(context)
    composeView.setContent {
        com.app.codebuzz.flipquotes.ui.components.ShareableQuoteCard(quote = quote)
    }

    // Get device width and a proportional height for the card
    val displayMetrics = context.resources.displayMetrics
    val width = displayMetrics.widthPixels
    val height = (width * 1.5).toInt() // Adjust this ratio as needed for your card

    val container = android.widget.FrameLayout(context)
    container.addView(composeView)
    activity.addContentView(container, android.view.ViewGroup.LayoutParams(0, 0)) // Invisible

    composeView.post {
        val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY)
        val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, width, height)
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        composeView.draw(canvas)

        val resolver = context.contentResolver
        val filename = "Quote_${System.currentTimeMillis()}.jpg"
        val contentValues = android.content.ContentValues().apply {
            put(Media.DISPLAY_NAME, filename)
            put(Media.MIME_TYPE, "image/jpeg")
            put(Media.RELATIVE_PATH, "Pictures/FlipQuotes")
            put(Media.IS_PENDING, 1)
        }
        val imageUri = resolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            var out: java.io.OutputStream? = null
            try {
                out = resolver.openOutputStream(imageUri)
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
            } finally {
                out?.close()
            }
            contentValues.clear()
            contentValues.put(Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_TEXT, "For more amazing quotes check out https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes")
            }
            context.startActivity(Intent.createChooser(intent, "Share Quote from FlipQuotes"))
        }
        (container.parent as? android.view.ViewGroup)?.removeView(container)
    }
}
