package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

import com.app.codebuzz.flipquotes.R

@Composable
fun Header(
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {}
) {
    val padding = dimensionResource(id = R.dimen.padding)
    val marginTop = dimensionResource(id = R.dimen.marginTop)
    val subHeadingSize = dimensionResource(id = R.dimen.subHeading).value.sp
    val colorPrimary = colorResource(id = R.color.colorPrimary)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorPrimary)
            .padding(bottom = padding)
            .padding(top = marginTop, end = marginTop),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onSearchClick) {
                Image(
                    painter = painterResource(id = R.drawable.playist),
                    contentDescription = stringResource(id = R.string.menu),
                    modifier = Modifier.padding(padding)
                )
            }

            Text(
                text = stringResource(id = R.string.my_feed),
                color = Color.White,
                fontSize = subHeadingSize,
                fontFamily = FontFamily(Font(R.font.droid_sans)),
                maxLines = 1,
                modifier = Modifier.padding(padding)
            )
        }
        // Add refresh button on the top right
        IconButton(onClick = onRefreshClick) {
            Image(
                painter = painterResource(id = R.drawable.refresh),
                contentDescription = stringResource(id = R.string.refresh),
                modifier = Modifier.padding(padding)
            )
        }
    }
}
