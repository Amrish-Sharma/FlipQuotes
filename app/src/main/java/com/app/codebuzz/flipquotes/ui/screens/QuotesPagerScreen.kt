package com.app.codebuzz.flipquotes.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.content.ContentValues
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.ui.components.QuoteCard
import com.app.codebuzz.flipquotes.ui.components.Header
import com.app.codebuzz.flipquotes.ui.components.Footer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.OutputStream
import java.util.*
import androidx.core.net.toUri
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun QuotePagerScreen(viewModel: QuoteViewModel) {
    val quotes = viewModel.quotes
    var currentIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    // State for like/bookmark per quote
    val likeStates = remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }
    val bookmarkStates = remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }
    val likeCounts = remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    val isLiked = likeStates.value[currentIndex] ?: false
    val isBookmarked = bookmarkStates.value[currentIndex] ?: false
    val likeCount = likeCounts.value[currentIndex] ?: 0

    if (quotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val view = LocalView.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentIndex, quotes.size) {
                    detectVerticalDragGestures { change, dragAmount ->
                        if (dragAmount < -50) { // swipe up
                            if (currentIndex < quotes.size - 1) currentIndex++
                            else currentIndex = 0
                        } else if (dragAmount > 50) { // swipe down
                            if (currentIndex > 0) currentIndex--
                            else currentIndex = quotes.size - 1
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            QuoteCard(
                quote = quotes[currentIndex],
                onRefreshClick = {
                    currentIndex = (currentIndex + 1) % quotes.size
                },
                onShareClick = {
                    shareQuoteImage(context, view, quotes[currentIndex])
                },
                onFlip = {
                    if (currentIndex < quotes.size - 1) currentIndex++
                    else currentIndex = 0
                },
                header = {
                    Header(
                        onSearchClick = {},
                        onRefreshClick = {
                            currentIndex = (currentIndex + 1) % quotes.size
                        }
                    )
                },
                footer = {
                    Footer(
                        likeCount = likeCount.toString(),
                        isLiked = isLiked,
                        isBookmarked = isBookmarked,
                        onLikeClick = {
                            likeStates.value = likeStates.value.toMutableMap().apply {
                                set(currentIndex, !isLiked)
                            }
                            likeCounts.value = likeCounts.value.toMutableMap().apply {
                                set(currentIndex, if (!isLiked) likeCount + 1 else (likeCount - 1).coerceAtLeast(0))
                            }
                        },
                        onShareClick = {
                            shareQuoteImage(context, view, quotes[currentIndex])
                        },
                        onBookmarkClick = {
                            bookmarkStates.value = bookmarkStates.value.toMutableMap().apply {
                                set(currentIndex, !isBookmarked)
                            }
                        }
                    )
                }
            )
        }
    }
}

fun shareQuoteImage(context: Context, view: android.view.View?, quote: Quote) {
    // Share only the quote content (not header/footer)
    val activity = context as? Activity ?: return
    val composeView = ComposeView(context)
    composeView.setContent {
        com.app.codebuzz.flipquotes.ui.components.QuoteContent(quote)
    }
    // Measure and layout the ComposeView
    val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(1080, android.view.View.MeasureSpec.EXACTLY)
    val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(400, android.view.View.MeasureSpec.AT_MOST)
    composeView.measure(widthSpec, heightSpec)
    composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
    val bitmap = createBitmap(composeView.width, composeView.height)
    val canvas = android.graphics.Canvas(bitmap)
    composeView.draw(canvas)

    val resolver = context.contentResolver
    val filename = "Quote_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(Media.DISPLAY_NAME, filename)
        put(Media.MIME_TYPE, "image/jpeg")
        put(Media.RELATIVE_PATH, "Pictures/FlipQuotes")
        put(Media.IS_PENDING, 1)
    }
    val imageUri = resolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)
    if (imageUri != null) {
        var out: OutputStream? = null
        try {
            out = resolver.openOutputStream(imageUri)
            if (out != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } finally {
            out?.close()
        }
        contentValues.clear()
        contentValues.put(Media.IS_PENDING, 0)
        resolver.update(imageUri, contentValues, null, null)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_TEXT, "For more amazing quotes check out https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes")
        }
        context.startActivity(Intent.createChooser(intent, "Share Quote from FlipQuotes"))
    }
}

@Composable
fun getViewModelCompat(): QuoteViewModel {
    // Fallback for Compose versions where viewModel() is not available
    return remember { QuoteViewModel() }
}

class QuoteViewModel : ViewModel() {
    private val _quotes = mutableStateListOf<Quote>()
    val quotes: List<Quote> get() = _quotes

    init {
        fetchQuotes()
    }

    private fun fetchQuotes() {
        viewModelScope.launch(Dispatchers.IO) {
            val url = "https://raw.githubusercontent.com/Amrish-Sharma/fq_quotes/refs/heads/main/Quotes.json"
            val request = Request.Builder().url(url).build()
            val response = OkHttpClient().newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { json ->
                    val listType = object : TypeToken<List<Quote>>() {}.type
                    val loadedQuotes: List<Quote> = Gson().fromJson(json, listType)
                    _quotes.clear()
                    _quotes.addAll(loadedQuotes.shuffled())
                }
            }
        }
    }
}

@Composable
fun QuotePagerHeader() {
    Text(
        text = "FlipQuotes",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun QuotePagerFooter(onRefreshClick: () -> Unit, onShareClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(onClick = onRefreshClick) { Text("Refresh") }
        Button(onClick = onShareClick) { Text("Share") }
    }
}
