package com.app.codebuzz.flipquotes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    quotes: List<Quote>,
    onQuoteSelected: (Quote) -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier,
    onBookmarkToggle: (Quote) -> Unit = {},
    isQuoteBookmarked: (Quote) -> Boolean = { false }
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Popular search suggestions
    val popularSearchTerms = listOf(
        "motivational", "love", "success", "life", "happiness", "wisdom",
        "inspiration", "hope", "friendship", "dreams", "courage", "peace"
    )

    // Results update as the user types; null means there is no query yet
    val results by produceState<List<Quote>?>(initialValue = null, searchQuery, quotes) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            value = null
        } else {
            delay(250)
            value = withContext(Dispatchers.Default) {
                quotes.filter {
                    it.quote.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true) ||
                    it.theme.contains(query, ignoreCase = true)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    stringResource(R.string.search_hint),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light, fontSize = 16.sp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.accentColor,
                cursorColor = theme.accentColor
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() }
            ),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search)
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content Area
        val currentResults = results
        when {
            currentResults == null -> {
                Text(
                    text = stringResource(R.string.popular_searches),
                    color = theme.accentColor,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    popularSearchTerms.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { searchQuery = suggestion },
                            label = { Text(suggestion, style = MaterialTheme.typography.labelLarge) }
                        )
                    }
                }
                EmptyState(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.search_prompt),
                    message = stringResource(R.string.search_prompt_hint),
                    theme = theme
                )
            }
            currentResults.isEmpty() -> {
                EmptyState(
                    icon = null,
                    title = stringResource(R.string.no_results),
                    message = stringResource(R.string.no_results_hint),
                    theme = theme
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp) // Extra padding for last item
                ) {
                    items(currentResults) { quote ->
                        QuoteListItem(
                            quote = quote,
                            isBookmarked = isQuoteBookmarked(quote),
                            onQuoteClick = {
                                keyboardController?.hide()
                                onQuoteSelected(quote)
                            },
                            onBookmarkClick = { onBookmarkToggle(quote) },
                            theme = theme
                        )
                    }
                }
            }
        }
    }
}

// Centered placeholder used by the search and saved lists
@Composable
internal fun EmptyState(
    icon: ImageVector?,
    title: String,
    message: String,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = theme.onSurfaceColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                title,
                color = theme.onSurfaceColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                color = theme.onSurfaceColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Quote row shared by search results and the saved list
@Composable
internal fun QuoteListItem(
    quote: Quote,
    isBookmarked: Boolean,
    onQuoteClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onQuoteClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
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
                    color = theme.accentColor,
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

            // Bookmark button
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(
                        if (isBookmarked) R.string.remove_bookmark else R.string.add_bookmark
                    ),
                    tint = if (isBookmarked) theme.accentColor else theme.onSurfaceColor
                )
            }
        }
    }
}
