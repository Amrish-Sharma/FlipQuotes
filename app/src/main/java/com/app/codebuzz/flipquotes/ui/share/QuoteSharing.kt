package com.app.codebuzz.flipquotes.ui.share

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore.Images.Media
import androidx.core.content.FileProvider
import com.app.codebuzz.flipquotes.data.Quote
import java.io.File

private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.app.codebuzz.flipquotes"

// Shares via a private cache file so nothing is written to the user's gallery
fun shareQuoteImage(context: Context, quote: Quote, bitmap: Bitmap) {
    try {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "flipquote.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "For more amazing quotes check out FlipQuotes app: $PLAY_STORE_URL")
            putExtra(Intent.EXTRA_SUBJECT, "Inspiring Quote from FlipQuotes")
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    } catch (_: Exception) {
        shareQuoteAsText(context, quote)
    }
}

fun saveQuoteImageToGallery(context: Context, bitmap: Bitmap, quote: Quote): Uri? {
    return try {
        val contentValues = ContentValues().apply {
            put(Media.DISPLAY_NAME, "FlipQuotes_${System.currentTimeMillis()}.png")
            put(Media.MIME_TYPE, "image/png")
            put(Media.DESCRIPTION, "Quote: ${quote.quote.take(50)}... - ${quote.author}")
            put(Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/FlipQuotes")
            put(Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
        context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        contentValues.clear()
        contentValues.put(Media.IS_PENDING, 0)
        context.contentResolver.update(uri, contentValues, null, null)
        uri
    } catch (_: Exception) {
        null
    }
}

fun shareQuoteAsText(context: Context, quote: Quote) {
    try {
        val quoteText = "\"${quote.quote}\"\n\n~ ${quote.author}\n\nFor more amazing quotes check out FlipQuotes app: $PLAY_STORE_URL"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, quoteText)
            putExtra(Intent.EXTRA_SUBJECT, "Inspiring Quote from FlipQuotes")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    } catch (_: Exception) {
        // Silent fallback - prevent any crashes
    }
}
