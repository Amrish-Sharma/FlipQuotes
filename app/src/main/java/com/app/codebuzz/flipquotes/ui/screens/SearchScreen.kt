package com.app.codebuzz.flipquotes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.ui.theme.AppTheme

@Composable
fun SearchScreen(
    quotes: List<Quote>,
    onQuoteSelected: (Quote) -> Unit,
    onClose: () -> Unit,
    visible: Boolean,
    theme: AppTheme,
    bookmarkedQuotes: List<Quote> = emptyList(),
    onBookmarkToggle: (Quote) -> Unit = {},
    isQuoteBookmarked: (Quote) -> Boolean = { false }
) {
    var searchQuery by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Popular search suggestions
    val popularSearchTerms = listOf(
        "motivational", "love", "success", "life", "happiness", "wisdom",
        "inspiration", "hope", "friendship", "dreams", "courage", "peace"
    )

    // Filter suggestions based on current query
    val filteredSuggestions = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            popularSearchTerms.take(6)
        } else {
            popularSearchTerms.filter {
                it.contains(searchQuery, ignoreCase = true)
            }.take(6)
        }
    }

    // Define the search action function
    val performSearch = {
        if (searchQuery.isNotBlank()) {
            showResults = true
            showSuggestions = false
            showBookmarks = false
            keyboardController?.hide()
        }
    }

    val filteredQuotes = remember(searchQuery, quotes) {
        if (searchQuery.isBlank()) emptyList() else quotes.filter {
            it.quote.contains(searchQuery, ignoreCase = true) ||
            it.author.contains(searchQuery, ignoreCase = true) ||
            it.theme.contains(searchQuery, ignoreCase = true)
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(durationMillis = 350)
        )
    ) {
        Surface(
            color = theme.backgroundColor.copy(alpha = 0.98f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .windowInsetsPadding(WindowInsets.statusBars) // Add padding for status bar
                        .windowInsetsPadding(WindowInsets.navigationBars) // Add padding for system navigation
                ) {
                    // Enhanced Search Bar with Clear button and Bookmark button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                showResults = false
                                showBookmarks = false
                                showSuggestions = it.isNotBlank()
                            },
                            placeholder = {
                                Text(
                                    "Start typing to find inspiring quotes",
                                    color = theme.onSurfaceColor.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = theme.onSurfaceColor,
                                unfocusedTextColor = theme.onSurfaceColor,
                                focusedContainerColor = theme.surfaceColor.copy(alpha = 0.8f),
                                unfocusedContainerColor = theme.surfaceColor.copy(alpha = 0.8f),
                                focusedIndicatorColor = Color.Yellow,
                                unfocusedIndicatorColor = theme.onSurfaceColor.copy(alpha = 0.5f),
                                cursorColor = theme.onSurfaceColor,
                                focusedPlaceholderColor = theme.onSurfaceColor.copy(alpha = 0.6f),
                                unfocusedPlaceholderColor = theme.onSurfaceColor.copy(alpha = 0.6f)
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = theme.onSurfaceColor),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = { performSearch() }
                            ),
                            trailingIcon = {
                                // Cancel button - always visible, clears search and resets state
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        showResults = false
                                        showSuggestions = false
                                        showBookmarks = false
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = theme.onSurfaceColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Search button
                        IconButton(onClick = { performSearch() }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = theme.onSurfaceColor
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Bookmark button - moved from floating position to search bar row
                        IconButton(
                            onClick = {
                                showBookmarks = !showBookmarks
                                showResults = false
                                showSuggestions = false
                                if (showBookmarks) {
                                    searchQuery = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (showBookmarks) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Show bookmarked quotes",
                                tint = if (showBookmarks) Color.Yellow else theme.onSurfaceColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto-suggestions dropdown
                    if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = theme.surfaceColor.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Popular search terms:",
                                    color = Color.Yellow,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                filteredSuggestions.forEach { suggestion ->
                                    Text(
                                        text = suggestion,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchQuery = suggestion
                                                performSearch()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        color = theme.onSurfaceColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Content Area
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp) // Add bottom padding to account for footer height
                    ) {
                        when {
                            showResults -> {
                                if (filteredQuotes.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp) // Extra padding for last item
                                    ) {
                                        items(filteredQuotes) { quote ->
                                            QuoteCard(
                                                quote = quote,
                                                isBookmarked = isQuoteBookmarked(quote),
                                                onQuoteClick = { onQuoteSelected(quote) },
                                                onBookmarkClick = { onBookmarkToggle(quote) },
                                                theme = theme
                                            )
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "No results found",
                                                color = theme.onSurfaceColor,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Try different keywords or check spelling",
                                                color = theme.onSurfaceColor.copy(alpha = 0.7f),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                            showBookmarks -> {
                                if (bookmarkedQuotes.isNotEmpty()) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 16.dp) // Extra padding for last item
                                    ) {
                                        items(bookmarkedQuotes) { quote ->
                                            QuoteCard(
                                                quote = quote,
                                                isBookmarked = true,
                                                onQuoteClick = { onQuoteSelected(quote) },
                                                onBookmarkClick = { onBookmarkToggle(quote) },
                                                theme = theme
                                            )
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Outlined.BookmarkBorder,
                                                contentDescription = "No bookmarks",
                                                tint = theme.onSurfaceColor.copy(alpha = 0.6f),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                "No bookmarked quotes yet",
                                                color = theme.onSurfaceColor,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Bookmark quotes to save them here",
                                                color = theme.onSurfaceColor.copy(alpha = 0.7f),
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                // Welcome placeholder message
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = theme.onSurfaceColor.copy(alpha = 0.6f),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Start typing to find inspiring quotes",
                                            color = theme.onSurfaceColor,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Search by quote, author, or theme",
                                            color = theme.onSurfaceColor.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(
    quote: Quote,
    isBookmarked: Boolean,
    onQuoteClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onQuoteClick() },
        colors = CardDefaults.cardColors(
            containerColor = theme.surfaceColor.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = quote.quote,
                    color = theme.onSurfaceColor,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "— ${quote.author}",
                    color = Color.Yellow,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quote.theme.uppercase(),
                    color = theme.onSurfaceColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Bookmark button
            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                    tint = if (isBookmarked) theme.onSurfaceColor else theme.onSurfaceColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
