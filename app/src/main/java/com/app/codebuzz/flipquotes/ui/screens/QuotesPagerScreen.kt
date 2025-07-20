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
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun QuotePagerScreen(viewModel: QuotesViewModel) {
    val quotes by viewModel.filteredQuotes.collectAsStateWithLifecycle(initialValue = emptyList())
    val allQuotes by viewModel.allQuotes.collectAsStateWithLifecycle(initialValue = emptyList())
    val themesList by viewModel.themesList.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle(initialValue = null)

    var currentQuoteIndex by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    // Fix: Use quote content as key instead of index to maintain bookmark state across theme changes
    val bookmarkStates = remember { mutableStateMapOf<String, Boolean>() }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { themesList.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Smooth entrance animation state
    var isAppVisible by remember { mutableStateOf(false) }

    // SEARCH STATE
    var showSearch by remember { mutableStateOf(false) }
    var selectedQuoteFromSearch by remember { mutableStateOf<Quote?>(null) }

    // MENU STATE
    var showMenu by remember { mutableStateOf(false) }

    // Handle search result selection
    fun onQuoteSelectedFromSearch(quote: Quote) {
        // Always select 'All' theme so the quote is visible regardless of previous theme
        viewModel.setSelectedTheme("All")
        showSearch = false
        selectedQuoteFromSearch = quote
    }

    // Handle quote selection from search with LaunchedEffect
    LaunchedEffect(selectedQuoteFromSearch) {
        selectedQuoteFromSearch?.let { quote ->
            val filteredQuotes = viewModel.filteredQuotes.value
            val idx = filteredQuotes.indexOfFirst { it == quote }
            if (idx >= 0) {
                currentQuoteIndex = idx
            }
            selectedQuoteFromSearch = null // Reset after processing
        }
    }

    // Trigger smooth fade-in after a short delay to ensure everything is positioned
    LaunchedEffect(quotes, themesList) {
        if (quotes.isNotEmpty() && themesList.isNotEmpty() && !isAppVisible) {
            delay(100) // Small delay for smooth positioning
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Main app content
            // When search is open, hide header/tabs and show home button in footer
            Scaffold(
                topBar = {
                    if (!showSearch) {
                        Column {
                            Header(
                                onRefreshClick = {
                                    isRefreshing = true
                                    viewModel.forceRefresh()
                                    isRefreshing = false
                                    currentQuoteIndex = 0
                                },
                                onMenuClick = { showMenu = true }
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
                    }
                },
                bottomBar = {
                    if (quotes.isNotEmpty()) {
                        // Get the current quote to check its bookmark status
                        val currentQuote = quotes[safeQuoteIndex]
                        val currentQuoteKey = "${currentQuote.quote}_${currentQuote.author}"

                        QuoteFooter(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            // Fix: Don't show bookmark when search is open, use actual current quote's bookmark status
                            isBookmarked = if (showSearch) false else bookmarkStates[currentQuoteKey] == true,
                            isHome = showSearch, // Show home button when search is open
                            onHomeClick = { showSearch = false }, // Home returns to main screen
                            onShareClick = {
                                shareQuoteImage(context, quotes[safeQuoteIndex])
                            },
                            onBookmarkClick = {
                                val quoteKey = "${currentQuote.quote}_${currentQuote.author}"
                                bookmarkStates[quoteKey] = bookmarkStates[quoteKey] != true
                            },
                            onSearchClick = { showSearch = true }
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

                // Search overlay - appears on top of everything
                if (showSearch) {
                    SearchScreen(
                        quotes = allQuotes,
                        onQuoteSelected = { quote ->
                            onQuoteSelectedFromSearch(quote)
                        },
                        onClose = { showSearch = false },
                        visible = showSearch,
                        bookmarkedQuotes = allQuotes.filter { quote ->
                            val quoteKey = "${quote.quote}_${quote.author}"
                            bookmarkStates[quoteKey] == true
                        },
                        onBookmarkToggle = { quote ->
                            val quoteKey = "${quote.quote}_${quote.author}"
                            bookmarkStates[quoteKey] = bookmarkStates[quoteKey] != true
                        },
                        isQuoteBookmarked = { quote ->
                            val quoteKey = "${quote.quote}_${quote.author}"
                            bookmarkStates[quoteKey] == true
                        }
                    )
                }
            }

            // Animated Menu Screen overlay that slides in from left to right
            AnimatedVisibility(
                visible = showMenu,
                enter = slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { -it } // Start from left edge (negative width)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it } // Exit to left edge
                ) + fadeOut(animationSpec = tween(300))
            ) {
                MenuScreen(
                    onBackClick = { showMenu = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun shareQuoteImage(context: Context, quote: Quote) {
    val activity = context as? Activity ?: return

    // Create a ComposeView to render the quote
    val composeView = ComposeView(context).apply {
        setContent {
            QuoteCard(quote = quote)
        }
    }

    // Measure and layout the ComposeView
    composeView.measure(
        android.view.View.MeasureSpec.makeMeasureSpec(800, android.view.View.MeasureSpec.EXACTLY),
        android.view.View.MeasureSpec.makeMeasureSpec(600, android.view.View.MeasureSpec.EXACTLY)
    )
    composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

    // Create bitmap from the view
    val bitmap = createBitmap(composeView.measuredWidth, composeView.measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    composeView.draw(canvas)

    // Save bitmap and share
    try {
        val imageUri = Media.insertImage(context.contentResolver, bitmap, "FlipQuotes_${System.currentTimeMillis()}", "Quote from FlipQuotes")

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri.toUri())
            putExtra(Intent.EXTRA_TEXT, "\"${quote.quote}\" - ${quote.author}\n\nShared via FlipQuotes")
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        activity.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    } catch (e: Exception) {
        // Fallback to text sharing if image sharing fails
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "\"${quote.quote}\" - ${quote.author}\n\nShared via FlipQuotes")
            type = "text/plain"
        }
        activity.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    }
}
