package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.ui.theme.AppTheme
import com.app.codebuzz.flipquotes.ui.theme.AppThemes

@Composable
fun QuoteFooter(
    modifier: Modifier = Modifier,
    theme: AppTheme,
    isBookmarked: Boolean = false,
    isHome: Boolean = false,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}, // NEW: search button callback
    onBookmarkClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(), // Remove bottom padding
        color = theme.primaryColor,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // HOME or SEARCH BUTTON (leftmost)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.height(64.dp)
            ) {
                Spacer(modifier = Modifier.height(22.dp))
                if (isHome) {
                    FooterIconButton(
                        icon = Icons.Filled.Home,
                        contentDescription = "Go Home",
                        onClick = onHomeClick,
                        isActive = false,
                        theme = theme
                    )
                } else {
                    FooterIconButton(
                        icon = Icons.Filled.Search,
                        contentDescription = "Search quotes",
                        onClick = onSearchClick,
                        isActive = false,
                        theme = theme
                    )
                }
            }
            // BOOKMARK BUTTON (center)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.height(64.dp)
            ) {
                Spacer(modifier = Modifier.height(22.dp))
                FooterIconButton(
                    icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                    onClick = onBookmarkClick,
                    isActive = isBookmarked,
                    theme = theme
                )
            }
            // SHARE BUTTON (rightmost)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.height(64.dp)
            ) {
                Spacer(modifier = Modifier.height(22.dp))
                FooterIconButton(
                    icon = Icons.Filled.Share,
                    contentDescription = "Share quote",
                    onClick = onShareClick,
                    theme = theme
                )
            }
        }
    }
}

@Composable
private fun FooterIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    theme: AppTheme,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) theme.onSurfaceColor else theme.onPrimaryColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview
@Composable
fun QuoteFooterPreview() {
    QuoteFooter(
        theme = AppThemes.WhiteTheme,
        isBookmarked = true
    )
}
