package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.codebuzz.flipquotes.R

@Composable
fun Footer(
    modifier: Modifier = Modifier,
    likeCount: String = "0",
    isLiked: Boolean = false,
    isBookmarked: Boolean = false,
    onLikeClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {}
) {
    val padding = dimensionResource(id = R.dimen.padding)
    val marginTop = dimensionResource(id = R.dimen.marginTop)
    val iconSize = dimensionResource(id = R.dimen.iconDimen)
    val moreShort = dimensionResource(id = R.dimen.moreShort).value.sp
    val colorPrimary = colorResource(id = R.color.colorPrimary)

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFF111111)
        )

        Row(
            modifier = Modifier
                .background(colorPrimary)
                .fillMaxWidth()
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Like Section
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = marginTop),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onLikeClick) {
                    Image(
                        painter = painterResource(
                            id = if (isLiked) R.drawable.thumb_up else R.drawable.thumb_up_outline
                        ),
                        contentDescription = stringResource(R.string.like),
                        modifier = Modifier.size(iconSize)
                    )
                }
                Text(
                    text = likeCount,
                    color = Color.White,
                    fontSize = moreShort,
                    modifier = Modifier.padding(start = marginTop),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Share Section
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = marginTop),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onShareClick) {
                    Image(
                        painter = painterResource(id = R.drawable.share_variant),
                        contentDescription = stringResource(R.string.share),
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            // Bookmark Section
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = marginTop),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onBookmarkClick) {
                    Image(
                        painter = painterResource(
                            id = if (isBookmarked) R.drawable.bookmark else R.drawable.bookmark_outline
                        ),
                        contentDescription = stringResource(R.string.bookmark),
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}
