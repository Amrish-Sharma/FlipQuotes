package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuoteFooter(
    modifier: Modifier = Modifier,
    isBookmarked: Boolean = false,
    isLiked: Boolean = false,
    likeCount: String = "0",
    onBookmarkClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = Color.Black,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom // Align all items to bottom
        ) {
            // Bookmark button - wrap in Column for consistent height
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.height(64.dp) // Fixed height for all columns
            ) {
                Spacer(modifier = Modifier.height(22.dp)) // Placeholder for consistent spacing
                FooterIconButton(
                    icon = if (isBookmarked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                    onClick = onBookmarkClick,
                    isActive = isBookmarked
                )
            }

            // Like button with count - Stack vertically for better visibility
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.height(64.dp) // Fixed height for all columns
            ) {
                // Count positioned above the button
                if (isLiked || (likeCount.toIntOrNull() != null && likeCount.toInt() > 0)) {
                    Text(
                        text = likeCount,
                        color = Color.White,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                } else {
                    // Placeholder to maintain consistent spacing
                    Spacer(modifier = Modifier.height(22.dp))
                }

                FooterIconButton(
                    icon = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    onClick = onLikeClick,
                    isActive = isLiked
                )
            }

            // Share button - wrap in Column for consistent height
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.height(64.dp) // Fixed height for all columns
            ) {
                Spacer(modifier = Modifier.height(22.dp)) // Placeholder for consistent spacing
                FooterIconButton(
                    icon = Icons.Filled.Share,
                    contentDescription = "Share quote",
                    onClick = onShareClick
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
            tint = if (isActive) Color.Yellow else Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
