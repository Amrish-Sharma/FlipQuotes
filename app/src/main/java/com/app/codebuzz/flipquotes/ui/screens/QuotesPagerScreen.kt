package com.app.codebuzz.flipquotes.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore.Images.Media
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.DelicateCoroutinesApi
import androidx.core.graphics.createBitmap

@OptIn(DelicateCoroutinesApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun QuotePagerScreen(viewModel: QuoteViewModel) {
    val quotes = viewModel.quotes
    var currentIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val likeStates = remember { mutableStateMapOf<Int, Boolean>() }
    val bookmarkStates = remember { mutableStateMapOf<Int, Boolean>() }
    val likeCounts = remember { mutableStateMapOf<Int, Int>() }
    val isLiked = likeStates[currentIndex] == true
    val isBookmarked = bookmarkStates[currentIndex] == true
    val likeCount = likeCounts[currentIndex] ?: 0
    var swipeDirection by remember { mutableStateOf(0) } // 1 for next, -1 for previous
    // Blur overlay state (disabled until modern solution is available)
    rememberCoroutineScope()

    if (quotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Blur overlay is disabled for now
            /*
            if (showBlur && blurBitmapState != null) {
                androidx.compose.foundation.Image(
                    bitmap = blurBitmapState!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            */
            QuoteCard(
                quote = quotes[currentIndex],
                swipeDirection = swipeDirection,
                onNext = {
                    swipeDirection = 1
                    currentIndex = (currentIndex + 1) % quotes.size
                },
                onPrevious = {
                    swipeDirection = -1
                    currentIndex = (currentIndex - 1 + quotes.size) % quotes.size
                },
                header = {
                    Header(
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
                            likeStates[currentIndex] = !isLiked
                            likeCounts[currentIndex] = if (!isLiked) likeCount + 1 else (likeCount - 1).coerceAtLeast(0)
                        },
                        onShareClick = {
                            shareQuoteImage(context, quotes[currentIndex])
                        },
                        onBookmarkClick = {
                            bookmarkStates[currentIndex] = !isBookmarked
                        }
                    )
                }
            )
        }
    }
}

fun shareQuoteImage(context: Context, quote: Quote) {
    val activity = context as? Activity ?: return
    val composeView = ComposeView(context)
    composeView.setContent {
        QuoteCard(
            quote = quote,
            onNext = {},
            header = {},
            footer = {}
        )
    }

    // Get device width and a proportional height for the card
    val displayMetrics = context.resources.displayMetrics
    val width = displayMetrics.widthPixels
    val height = (width * 1.5).toInt() // Adjust this ratio as needed for your card

    val container = android.widget.FrameLayout(context)
    container.addView(composeView)
    activity.addContentView(container, android.view.ViewGroup.LayoutParams(0, 0)) // Invisible

    composeView.post {
        val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY)
        val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, width, height)
        val bitmap = createBitmap(width, height)
        val canvas = android.graphics.Canvas(bitmap)
        composeView.draw(canvas)

        val resolver = context.contentResolver
        val filename = "Quote_${System.currentTimeMillis()}.jpg"
        val contentValues = android.content.ContentValues().apply {
            put(Media.DISPLAY_NAME, filename)
            put(Media.MIME_TYPE, "image/jpeg")
            put(Media.RELATIVE_PATH, "Pictures/FlipQuotes")
            put(Media.IS_PENDING, 1)
        }
        val imageUri = resolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            var out: java.io.OutputStream? = null
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
        (container.parent as? android.view.ViewGroup)?.removeView(container)
    }
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
