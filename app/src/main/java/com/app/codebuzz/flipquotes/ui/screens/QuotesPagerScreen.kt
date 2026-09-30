package com.app.codebuzz.flipquotes.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.data.key
import com.app.codebuzz.flipquotes.ui.components.DailyQuoteSheet
import com.app.codebuzz.flipquotes.ui.components.Header
import com.app.codebuzz.flipquotes.ui.components.QuoteCard
import com.app.codebuzz.flipquotes.ui.components.QuoteFooter
import com.app.codebuzz.flipquotes.ui.share.ShareSheet
import com.app.codebuzz.flipquotes.ui.theme.rememberThemeManager
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun QuotePagerScreen(
    viewModel: QuotesViewModel,
    openDailyQuoteRequest: Boolean = false,
    onDailyQuoteRequestHandled: () -> Unit = {}
) {
    val quotes by viewModel.filteredQuotes.collectAsStateWithLifecycle(initialValue = emptyList())
    val allQuotes by viewModel.allQuotes.collectAsStateWithLifecycle(initialValue = emptyList())
    val themesList by viewModel.themesList.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle(initialValue = null)
    // Persisted bookmarks, keyed by Quote.key
    val bookmarkedKeys by viewModel.bookmarkedKeys.collectAsStateWithLifecycle()
    val dailyQuote by viewModel.dailyQuote.collectAsStateWithLifecycle()

    // Add theme manager
    val themeManager = rememberThemeManager()
    val currentTheme by themeManager.currentTheme
    val quoteFont by themeManager.quoteFont

    var currentQuoteIndex by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(value = false) }

    val pagerState = rememberPagerState(initialPage = 0) { themesList.size }
    val coroutineScope = rememberCoroutineScope()

    // QUOTE OF THE DAY / SHARE SHEET STATE
    var showDailyQuote by remember { mutableStateOf(false) }
    var quoteToShare by remember { mutableStateOf<Quote?>(null) }

    // Smooth entrance animation state
    var isAppVisible by remember { mutableStateOf(value = false) }

    // SEARCH STATE
    var showSearch by remember { mutableStateOf(false) }
    var selectedQuoteFromSearch by remember { mutableStateOf<Quote?>(null) }

    // MENU STATE
    var showMenu by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Handle search result selection
    fun onQuoteSelectedFromSearch(quote: Quote) {
        // Always select 'All' theme so the quote is visible regardless of previous theme
        viewModel.setSelectedTheme("All")
        showSearch = false
        selectedQuoteFromSearch = quote
    }

    // Handle quote selection from search with LaunchedEffect
    LaunchedEffect(selectedQuoteFromSearch) {
        selectedQuoteFromSearch?.let { quote: Quote ->
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
            delay(100.milliseconds) // Small delay for smooth positioning
            isAppVisible = true
        }
    }

    // Show the Quote of the Day on the first open of each day
    LaunchedEffect(isAppVisible) {
        if (isAppVisible && viewModel.shouldAutoShowDailyQuote()) {
            viewModel.loadDailyQuote()
            showDailyQuote = true
        }
    }

    // Opened from the daily notification or the home-screen widget
    LaunchedEffect(openDailyQuoteRequest) {
        if (openDailyQuoteRequest) {
            viewModel.loadDailyQuote()
            showDailyQuote = true
            onDailyQuoteRequestHandled()
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
                ) { -50 }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main app content
            // When search is open, hide header/tabs and show home button in footer
            Scaffold(
                topBar = {
                    if (!showSearch) {
                        Column {
                            Header(
                                theme = currentTheme,
                                onRefreshClick = {
                                    isRefreshing = true
                                    viewModel.forceRefresh()
                                    isRefreshing = false
                                    currentQuoteIndex = 0
                                },
                                onMenuClick = { showMenu = true }
                            )

                            if (themesList.isNotEmpty()) {
                                SecondaryScrollableTabRow(
                                    selectedTabIndex = pagerState.currentPage,
                                    edgePadding = 12.dp,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    indicator = {
                                        TabRowDefaults.SecondaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                                            color = MaterialTheme.colorScheme.primary
                                        )
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

                        QuoteFooter(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            theme = currentTheme,
                            // Don't show bookmark when search is open, use actual current quote's bookmark status
                            isBookmarked = !showSearch && currentQuote.key in bookmarkedKeys,
                            isHome = showSearch, // Show home button when search is open
                            onHomeClick = { showSearch = false }, // Home returns to main screen
                            onShareClick = { quoteToShare = currentQuote },
                            onBookmarkClick = { viewModel.toggleBookmark(currentQuote) },
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
                        ) { _ ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                QuoteCard(
                                    quote = quotes[safeQuoteIndex],
                                    themeManager = themeManager,
                                    isRefreshing = isRefreshing,
                                    onNext = {
                                        if (quotes.isNotEmpty()) {
                                            currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
                                        }
                                    },
                                    onPrevious = {
                                        if (quotes.isNotEmpty()) {
                                            currentQuoteIndex = ((currentQuoteIndex - 1) + quotes.size) % quotes.size
                                        }
                                    },
                                    onMenuOpen = { showMenu = true },
                                    onThemeNext = {
                                        // Navigate to next theme if available
                                        if (pagerState.currentPage < (themesList.size - 1)) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    },
                                    onThemePrevious = {
                                        // Navigate to previous theme if available
                                        if (pagerState.currentPage > 0) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        }
                                    },
                                    isOnAllThemes = selectedTheme == "All"
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
                        visible = true,
                        theme = currentTheme,
                        bookmarkedQuotes = allQuotes.filter { quote: Quote -> quote.key in bookmarkedKeys },
                        onBookmarkToggle = { quote -> viewModel.toggleBookmark(quote) },
                        isQuoteBookmarked = { quote -> quote.key in bookmarkedKeys }
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
                    onSettingsClick = {
                        // Don't hide menu when opening settings - keep it underneath
                        showSettings = true
                    },
                    onDailyQuoteClick = {
                        viewModel.loadDailyQuote()
                        showDailyQuote = true
                    },
                    theme = currentTheme,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Settings Screen overlay - appears on top of Menu screen
            if (showSettings) {
                SettingsScreen(
                    onBackClick = {
                        // Only close settings, menu remains visible underneath
                        showSettings = false
                    },
                    themeManager = themeManager,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Quote of the Day sheet
    if (showDailyQuote) {
        val today = dailyQuote
        DailyQuoteSheet(
            quote = today,
            isBookmarked = today != null && today.key in bookmarkedKeys,
            theme = currentTheme,
            themeManager = themeManager,
            onBookmarkClick = { today?.let { viewModel.toggleBookmark(it) } },
            onShareClick = {
                showDailyQuote = false
                quoteToShare = today
            },
            onDismiss = { showDailyQuote = false }
        )
    }

    // Styled share sheet
    quoteToShare?.let { quote ->
        ShareSheet(
            quote = quote,
            quoteFont = quoteFont,
            theme = currentTheme,
            onDismiss = { quoteToShare = null }
        )
    }
}
