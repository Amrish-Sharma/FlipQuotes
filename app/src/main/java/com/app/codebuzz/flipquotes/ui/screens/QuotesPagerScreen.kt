package com.app.codebuzz.flipquotes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.data.key
import com.app.codebuzz.flipquotes.ui.components.AppDestination
import com.app.codebuzz.flipquotes.ui.components.DailyQuoteSheet
import com.app.codebuzz.flipquotes.ui.components.Header
import com.app.codebuzz.flipquotes.ui.components.QuoteActions
import com.app.codebuzz.flipquotes.ui.components.QuoteFooter
import com.app.codebuzz.flipquotes.ui.components.QuotePager
import com.app.codebuzz.flipquotes.ui.components.SwipeHint
import com.app.codebuzz.flipquotes.ui.share.ShareSheet
import com.app.codebuzz.flipquotes.ui.theme.ThemeManager
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesUiState
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesViewModel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuotePagerScreen(
    viewModel: QuotesViewModel,
    themeManager: ThemeManager,
    openDailyQuoteRequest: Boolean = false,
    onDailyQuoteRequestHandled: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allQuotes by viewModel.allQuotes.collectAsStateWithLifecycle()
    val themesList by viewModel.themesList.collectAsStateWithLifecycle()
    val quotesByTheme by viewModel.quotesByTheme.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    // Persisted bookmarks, keyed by Quote.key
    val bookmarkedKeys by viewModel.bookmarkedKeys.collectAsStateWithLifecycle()
    val dailyQuote by viewModel.dailyQuote.collectAsStateWithLifecycle()

    val currentTheme by themeManager.currentTheme
    val quoteFont by themeManager.quoteFont
    val swipeHintSeen by themeManager.swipeHintSeen

    // Theme tabs page horizontally; each tab owns a vertical pager over its quotes
    val pagerState = rememberPagerState(initialPage = 0) { themesList.size }
    val quotePagerStates = themesList.map { theme ->
        key(theme) { rememberPagerState { quotesByTheme[theme].orEmpty().size } }
    }
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    var destination by rememberSaveable { mutableStateOf(AppDestination.Home) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    // QUOTE OF THE DAY / SHARE SHEET STATE
    var showDailyQuote by remember { mutableStateOf(false) }
    var quoteToShare by remember { mutableStateOf<Quote?>(null) }

    // Quote picked in Search or Saved, waiting to be brought on screen
    var quoteToShow by remember { mutableStateOf<Quote?>(null) }

    val currentQuotes = themesList.getOrNull(pagerState.currentPage)?.let { quotesByTheme[it] }.orEmpty()
    val currentQuotePagerState = quotePagerStates.getOrNull(pagerState.currentPage)
    val currentQuote = currentQuotePagerState?.let { currentQuotes.getOrNull(it.currentPage) }
    val bookmarkedQuotes = remember(allQuotes, bookmarkedKeys) {
        allQuotes.filter { it.key in bookmarkedKeys }.distinctBy { it.key }
    }

    // Show the Quote of the Day on the first open of each day
    LaunchedEffect(uiState) {
        if (uiState == QuotesUiState.Ready && viewModel.shouldAutoShowDailyQuote()) {
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

    val quotesUpdatedMessage = stringResource(R.string.quotes_updated)
    val refreshFailedMessage = stringResource(R.string.refresh_failed)
    LaunchedEffect(Unit) {
        viewModel.refreshResults.collect { updated ->
            launch {
                snackbarHostState.showSnackbar(if (updated) quotesUpdatedMessage else refreshFailedMessage)
            }
        }
    }

    // Light tick whenever a new quote settles; the first swipe also retires the swipe hint
    LaunchedEffect(currentQuotePagerState) {
        val quotePagerState = currentQuotePagerState ?: return@LaunchedEffect
        snapshotFlow { quotePagerState.settledPage }.drop(1).collect {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            themeManager.markSwipeHintSeen()
        }
    }

    // Swiping right past the first tab opens the menu
    val openMenuOnOverscroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.x > 1f &&
                    drawerState.isClosed && !drawerState.isAnimationRunning
                ) {
                    coroutineScope.launch { drawerState.open() }
                }
                return Offset.Zero
            }
        }
    }

    BackHandler(enabled = destination != AppDestination.Home) {
        destination = AppDestination.Home
    }
    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen || destination == AppDestination.Home,
            drawerContent = {
                MenuDrawerContent(
                    onSettingsClick = {
                        // Don't hide menu when opening settings - keep it underneath
                        showSettings = true
                    },
                    onDailyQuoteClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.loadDailyQuote()
                        showDailyQuote = true
                    },
                    theme = currentTheme
                )
            }
        ) {
            Scaffold(
                topBar = {
                    when (destination) {
                        AppDestination.Home -> Column {
                            Header(
                                theme = currentTheme,
                                isRefreshing = isRefreshing,
                                onRefreshClick = { viewModel.forceRefresh() },
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
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
                        AppDestination.Saved -> TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.saved),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = currentTheme.primaryColor,
                                titleContentColor = currentTheme.onPrimaryColor
                            )
                        )
                        // Search brings its own search field
                        AppDestination.Search -> Unit
                    }
                },
                bottomBar = {
                    QuoteFooter(
                        theme = currentTheme,
                        destination = destination,
                        onDestinationSelected = { destination = it }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                when (destination) {
                    AppDestination.Home -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Bring a quote picked in Search or Saved on screen; every quote lives in the first ("All") tab
                        LaunchedEffect(quoteToShow) {
                            val quote = quoteToShow ?: return@LaunchedEffect
                            val index = quotesByTheme[QuotesViewModel.ALL_THEME].orEmpty().indexOf(quote)
                            if (index >= 0) {
                                pagerState.scrollToPage(0)
                                quotePagerStates.firstOrNull()?.scrollToPage(index)
                            }
                            quoteToShow = null
                        }

                        when {
                            themesList.isNotEmpty() -> {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(openMenuOnOverscroll)
                                ) { page ->
                                    val quotes = themesList.getOrNull(page)?.let { quotesByTheme[it] }.orEmpty()
                                    val quotePagerState = quotePagerStates.getOrNull(page)
                                    if (quotes.isEmpty() || quotePagerState == null) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(text = stringResource(R.string.no_quotes_for_theme))
                                        }
                                    } else {
                                        QuotePager(
                                            quotes = quotes,
                                            pagerState = quotePagerState,
                                            themeManager = themeManager
                                        )
                                    }
                                }

                                if (currentQuote != null && currentQuotePagerState != null) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.quote_position,
                                                currentQuotePagerState.currentPage + 1,
                                                currentQuotes.size
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        QuoteActions(
                                            isBookmarked = currentQuote.key in bookmarkedKeys,
                                            onBookmarkClick = { viewModel.toggleBookmark(currentQuote) },
                                            onShareClick = { quoteToShare = currentQuote }
                                        )
                                    }

                                    if (!swipeHintSeen) {
                                        SwipeHint(
                                            onDismiss = { themeManager.markSwipeHintSeen() },
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 72.dp)
                                        )
                                    }
                                }
                            }
                            uiState == QuotesUiState.Error -> LoadError(
                                onRetry = { viewModel.fetchQuotes() },
                                modifier = Modifier.align(Alignment.Center)
                            )
                            else -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    AppDestination.Search -> SearchScreen(
                        quotes = allQuotes,
                        onQuoteSelected = { quote ->
                            quoteToShow = quote
                            destination = AppDestination.Home
                        },
                        theme = currentTheme,
                        modifier = Modifier
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)
                            .imePadding(),
                        onBookmarkToggle = { quote -> viewModel.toggleBookmark(quote) },
                        isQuoteBookmarked = { quote -> quote.key in bookmarkedKeys }
                    )
                    AppDestination.Saved -> SavedScreen(
                        bookmarkedQuotes = bookmarkedQuotes,
                        onQuoteSelected = { quote ->
                            quoteToShow = quote
                            destination = AppDestination.Home
                        },
                        onBookmarkToggle = { quote -> viewModel.toggleBookmark(quote) },
                        theme = currentTheme,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }

        // Settings Screen overlay - appears on top of the menu
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

// Shown when there are no quotes at all: first launch with no connection and no cache
@Composable
private fun LoadError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.load_error_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.load_error_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}
