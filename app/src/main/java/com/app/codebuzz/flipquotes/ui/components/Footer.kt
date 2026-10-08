package com.app.codebuzz.flipquotes.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.ui.theme.AppTheme
import com.app.codebuzz.flipquotes.ui.theme.AppThemes

enum class AppDestination(@StringRes val label: Int, val icon: ImageVector) {
    Home(R.string.home, Icons.Filled.Home),
    Search(R.string.search, Icons.Filled.Search),
    Saved(R.string.saved, Icons.Filled.Bookmarks)
}

@Composable
fun QuoteFooter(
    modifier: Modifier = Modifier,
    theme: AppTheme,
    destination: AppDestination = AppDestination.Home,
    onDestinationSelected: (AppDestination) -> Unit = {}
) {
    NavigationBar(
        modifier = modifier,
        containerColor = theme.primaryColor,
        contentColor = theme.onPrimaryColor
    ) {
        AppDestination.entries.forEach { item ->
            NavigationBarItem(
                selected = item == destination,
                onClick = { onDestinationSelected(item) },
                icon = { Icon(imageVector = item.icon, contentDescription = null) },
                label = {
                    Text(
                        text = stringResource(item.label),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = theme.onPrimaryColor,
                    selectedTextColor = theme.onPrimaryColor,
                    indicatorColor = theme.onPrimaryColor.copy(alpha = 0.14f),
                    unselectedIconColor = theme.onPrimaryColor.copy(alpha = 0.6f),
                    unselectedTextColor = theme.onPrimaryColor.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Preview
@Composable
fun QuoteFooterPreview() {
    QuoteFooter(
        theme = AppThemes.WhiteTheme,
        destination = AppDestination.Saved
    )
}
