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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.ui.components.Header
import com.app.codebuzz.flipquotes.ui.components.QuoteCard
import com.app.codebuzz.flipquotes.ui.components.QuoteFooter
import com.app.codebuzz.flipquotes.ui.theme.rememberThemeManager
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun QuotePagerScreen(viewModel: QuotesViewModel) {
    val quotes by viewModel.filteredQuotes.collectAsStateWithLifecycle(initialValue = emptyList())
    val allQuotes by viewModel.allQuotes.collectAsStateWithLifecycle(initialValue = emptyList())
    val themesList by viewModel.themesList.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle(initialValue = null)

    // Add theme manager
    val themeManager = rememberThemeManager()
    val currentTheme by themeManager.currentTheme

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
                            theme = currentTheme,
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
                                    themeManager = themeManager,
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
                                    },
                                    onMenuOpen = { showMenu = true },
                                    onThemeNext = {
                                        // Navigate to next theme if available
                                        if (pagerState.currentPage < themesList.size - 1) {
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
                    onSettingsClick = {
                        // Don't hide menu when opening settings - keep it underneath
                        showSettings = true
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
}

fun shareQuoteImage(context: Context, quote: Quote) {
    val activity = context as? Activity ?: return

    try {
        // Create a bitmap using Canvas drawing instead of ComposeView
        val bitmap = createQuoteBitmapWithCanvas(context, quote)

        // Save the bitmap to device storage
        val imageUri = saveBitmapToDevice(context, bitmap, quote)

        if (imageUri != null) {
            // Create share intent with image and promotional text
            val promotionalText = "For more amazing quotes check out FlipQuotes app: https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes"

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, promotionalText)
                putExtra(Intent.EXTRA_SUBJECT, "Inspiring Quote from FlipQuotes")
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            activity.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
        } else {
            // Fallback to text sharing if image creation fails
            shareAsText(activity, quote)
        }

    } catch (_: Exception) {
        // Fallback to text sharing if anything fails
        shareAsText(activity, quote)
    }
}

private fun createQuoteBitmapWithCanvas(context: Context, quote: Quote): Bitmap {
    // Use portrait dimensions to match app layout
    val width = 600
    val height = 800
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    try {
        // Load and draw the texture background
        val textureDrawable = androidx.core.content.ContextCompat.getDrawable(context, com.app.codebuzz.flipquotes.R.drawable.texture)
        textureDrawable?.let { drawable ->
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)
        }
    } catch (_: Exception) {
        // Fallback background if texture fails to load
        val backgroundPaint = android.graphics.Paint().apply {
            color = "#F5F5DC".toColorInt() // Beige color
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
    }

    // Load custom fonts to match QuoteCard styling
    val quoteTypeface = try {
        androidx.core.content.res.ResourcesCompat.getFont(context, com.app.codebuzz.flipquotes.R.font.kotta_one)
    } catch (_: Exception) {
        android.graphics.Typeface.DEFAULT
    }

    val authorTypeface = try {
        androidx.core.content.res.ResourcesCompat.getFont(context, com.app.codebuzz.flipquotes.R.font.playfair_display)
    } catch (_: Exception) {
        android.graphics.Typeface.DEFAULT
    }

    val brandTypeface = try {
        androidx.core.content.res.ResourcesCompat.getFont(context, com.app.codebuzz.flipquotes.R.font.playfair_display)
    } catch (_: Exception) {
        android.graphics.Typeface.DEFAULT
    }

    // Quote text styling to match QuoteCard (headlineLarge with user's selected font)
    val quotePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 36f // Increase size to match app display better
        typeface = quoteTypeface
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        style = android.graphics.Paint.Style.FILL
    }

    // Split quote text into lines
    val maxWidth = width - 100 // Better padding for portrait
    val quoteText = "\"${quote.quote}\""
    val lines = wrapTextToLines(quoteText, quotePaint, maxWidth.toFloat())

    // Calculate vertical positioning for portrait layout
    val lineSpacing = 45f // Increase line spacing for better readability
    val totalTextHeight = lines.size * lineSpacing
    val startY = (height / 2 - totalTextHeight / 2) + 20f // Slightly adjust center position

    // Draw quote lines
    lines.forEachIndexed { index, line ->
        canvas.drawText(line, width / 2f, startY + index * lineSpacing, quotePaint)
    }

    // Author text styling to match QuoteCard (bodyLarge with playfair_display)
    val authorPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 28f // Increase size to match better
        typeface = authorTypeface
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawText("~ ${quote.author}", width / 2f, startY + lines.size * lineSpacing + 60f, authorPaint)

    // FlipQuotes watermark with Playfair Display font
    val brandPaint = android.graphics.Paint().apply {
        color = "#666666".toColorInt() // Slightly lighter gray for watermark effect
        textSize = 22f // Increase size for better visibility
        typeface = brandTypeface
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawText("FlipQuotes", width / 2f, height - 40f, brandPaint)

    return bitmap
}

private fun wrapTextToLines(text: String, paint: android.graphics.Paint, maxWidth: Float): List<String> {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""

    for (word in words) {
        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        val bounds = android.graphics.Rect()
        paint.getTextBounds(testLine, 0, testLine.length, bounds)

        if (bounds.width() <= maxWidth) {
            currentLine = testLine
        } else {
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
            currentLine = word
        }
    }

    if (currentLine.isNotEmpty()) {
        lines.add(currentLine)
    }

    return lines
}

private fun saveBitmapToDevice(context: Context, bitmap: Bitmap, quote: Quote): android.net.Uri? {
    return try {
        val filename = "FlipQuotes_${System.currentTimeMillis()}.png"
        val contentValues = android.content.ContentValues().apply {
            put(Media.DISPLAY_NAME, filename)
            put(Media.MIME_TYPE, "image/png")
            put(Media.DESCRIPTION, "Quote: ${quote.quote.take(50)}... - ${quote.author}")
            put(Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
            put(Media.IS_PENDING, 1)
        }

        val uri = context.contentResolver.insert(
            Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let { imageUri ->
            context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            // Mark as not pending on Android Q+
            contentValues.clear()
            contentValues.put(Media.IS_PENDING, 0)
            context.contentResolver.update(imageUri, contentValues, null, null)

            imageUri
        }
    } catch (_: Exception) {
        null
    }
}

private fun shareAsText(activity: Activity, quote: Quote) {
    try {
        val quoteText = "\"${quote.quote}\"\n\n~ ${quote.author}\n\nFor more amazing quotes check out FlipQuotes app: https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes"

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, quoteText)
            putExtra(Intent.EXTRA_SUBJECT, "Inspiring Quote from FlipQuotes")
            type = "text/plain"
        }

        activity.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    } catch (_: Exception) {
        // Silent fallback - prevent any crashes
    }
}
