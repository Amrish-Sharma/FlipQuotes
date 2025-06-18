package com.app.codebuzz.flipquotes.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore.Images.Media
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun QuotePagerScreen(viewModel: QuoteViewModel) {
    val quotes = viewModel.quotes
    var currentIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    // State for like/bookmark per quote
    val likeStates = remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }
    val bookmarkStates = remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }
    val likeCounts = remember { mutableStateOf(mutableMapOf<Int, Int>()) }
    val isLiked = likeStates.value[currentIndex] == true
    val isBookmarked = bookmarkStates.value[currentIndex] == true
    val likeCount = likeCounts.value[currentIndex] ?: 0

    // Blur overlay state
    var blurBitmapState by remember { mutableStateOf<Bitmap?>(null) }
    var showBlur by remember { mutableStateOf(false) }

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
                        if (!showBlur) {
                            // Capture and blur before transition
                            view.isDrawingCacheEnabled = true
                            val bitmap = Bitmap.createBitmap(view.drawingCache)
                            view.isDrawingCacheEnabled = false
                            blurBitmapState = com.app.codebuzz.flipquotes.blurBitmap(bitmap, 16)
                            showBlur = true
                            // Delay to show blur, then change index
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(200)
                                if (dragAmount < -50) { // swipe up
                                    if (currentIndex < quotes.size - 1) currentIndex++
                                    else currentIndex = 0
                                } else if (dragAmount > 50) { // swipe down
                                    if (currentIndex > 0) currentIndex--
                                    else currentIndex = quotes.size - 1
                                }
                                showBlur = false
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (showBlur && blurBitmapState != null) {
                androidx.compose.foundation.Image(
                    bitmap = blurBitmapState!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            QuoteCard(
                quote = quotes[currentIndex],
                onFlip = {
                    if (!showBlur) {
                        view.isDrawingCacheEnabled = true
                        val bitmap = Bitmap.createBitmap(view.drawingCache)
                        view.isDrawingCacheEnabled = false
                        blurBitmapState = com.app.codebuzz.flipquotes.blurBitmap(bitmap, 16)
                        showBlur = true
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(200)
                            if (currentIndex < quotes.size - 1) currentIndex++
                            else currentIndex = 0
                            showBlur = false
                        }
                    }
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
                            shareQuoteImage(context, quotes[currentIndex])
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

fun shareQuoteImage(context: Context, quote: Quote) {
    val activity = context as? Activity ?: return
    val composeView = ComposeView(context)
    composeView.setContent {
        com.app.codebuzz.flipquotes.ui.components.QuoteCard(
            quote = quote,
            onFlip = {},
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
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
