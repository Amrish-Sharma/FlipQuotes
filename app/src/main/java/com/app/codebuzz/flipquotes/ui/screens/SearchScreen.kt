package com.app.codebuzz.flipquotes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.app.codebuzz.flipquotes.data.Quote

@Composable
fun SearchScreen(
    quotes: List<Quote>,
    onQuoteSelected: (Quote) -> Unit,
    onClose: () -> Unit,
    visible: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }
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
            color = Color.Black.copy(alpha = 0.98f),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            showResults = false
                        },
                        label = { Text("Search quotes...", color = Color.White, fontSize = MaterialTheme.typography.bodySmall.fontSize) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp), // Increased height for a larger search bar
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.DarkGray,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedIndicatorColor = Color.Yellow,
                            unfocusedIndicatorColor = Color.Gray,
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White
                        ),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                    IconButton(onClick = { if (searchQuery.isNotBlank()) showResults = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (showResults) {
                    if (filteredQuotes.isNotEmpty()) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredQuotes) { quote ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onQuoteSelected(quote) },
                                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(quote.quote, color = Color.White)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("- ${quote.author}", color = Color.Yellow, style = MaterialTheme.typography.labelMedium)
                                        Text(quote.theme, color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No results found", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
