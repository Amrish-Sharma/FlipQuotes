package com.app.codebuzz.flipquotes.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.data.key
import com.app.codebuzz.flipquotes.ui.theme.AppTheme

@Composable
fun SavedScreen(
    bookmarkedQuotes: List<Quote>,
    onQuoteSelected: (Quote) -> Unit,
    onBookmarkToggle: (Quote) -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier
) {
    if (bookmarkedQuotes.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.BookmarkBorder,
            title = stringResource(R.string.no_saved_quotes),
            message = stringResource(R.string.no_saved_quotes_hint),
            theme = theme,
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            items(bookmarkedQuotes, key = { it.key }) { quote ->
                QuoteListItem(
                    quote = quote,
                    isBookmarked = true,
                    onQuoteClick = { onQuoteSelected(quote) },
                    onBookmarkClick = { onBookmarkToggle(quote) },
                    theme = theme,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}
