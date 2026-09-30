package com.app.codebuzz.flipquotes.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.data.Quote

enum class ShareFormat(val label: String, val width: Int, val height: Int) {
    PORTRAIT("Portrait", 1080, 1350),
    SQUARE("Square", 1080, 1080),
    STORY("Story", 1080, 1920)
}

// colors == null means the paper texture; one color is a solid fill; two or more is a diagonal gradient
enum class ShareBackground(
    val label: String,
    val colors: IntArray?,
    val textColor: Int,
    val authorColor: Int
) {
    PAPER("Paper", null, Color.BLACK, Color.DKGRAY),
    CREAM("Cream", intArrayOf(0xFFF5F0DC.toInt()), 0xFF2B2B2B.toInt(), 0xFF6B6250.toInt()),
    WHITE("White", intArrayOf(Color.WHITE), Color.BLACK, 0xFF555555.toInt()),
    BLACK("Black", intArrayOf(Color.BLACK), Color.WHITE, 0xFFBBBBBB.toInt()),
    SUNSET("Sunset", intArrayOf(0xFFFF7E5F.toInt(), 0xFF6A3093.toInt()), Color.WHITE, 0xFFF3E6FF.toInt()),
    OCEAN("Ocean", intArrayOf(0xFF2193B0.toInt(), 0xFF1B2A49.toInt()), Color.WHITE, 0xFFD6ECF3.toInt()),
    FOREST("Forest", intArrayOf(0xFF3A6B35.toInt(), 0xFF142A13.toInt()), Color.WHITE, 0xFFDDEBD9.toInt())
}

data class ShareStyle(
    val background: ShareBackground = ShareBackground.PAPER,
    val format: ShareFormat = ShareFormat.PORTRAIT
) {
    companion object {
        private const val PREFS_NAME = "share_prefs"

        fun load(context: Context): ShareStyle {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return ShareStyle(
                background = runCatching { ShareBackground.valueOf(prefs.getString("background", "")!!) }
                    .getOrDefault(ShareBackground.PAPER),
                format = runCatching { ShareFormat.valueOf(prefs.getString("format", "")!!) }
                    .getOrDefault(ShareFormat.PORTRAIT)
            )
        }
    }

    fun save(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString("background", background.name)
            putString("format", format.name)
        }
    }
}

object QuoteImageRenderer {

    fun render(context: Context, quote: Quote, style: ShareStyle, quoteFont: String): Bitmap {
        val width = style.format.width
        val height = style.format.height
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        drawBackground(context, canvas, style.background, width, height)

        val horizontalPadding = width * 0.1f
        val textWidth = (width - 2 * horizontalPadding).toInt()

        // Quote text - shrink until it fits comfortably for long quotes
        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.background.textColor
            typeface = typefaceFor(context, quoteFont)
            textSize = width * 0.065f
        }
        val maxQuoteHeight = height * 0.65f
        var quoteLayout = buildLayout("\"${quote.quote}\"", quotePaint, textWidth)
        while (quoteLayout.height > maxQuoteHeight && quotePaint.textSize > width * 0.03f) {
            quotePaint.textSize *= 0.92f
            quoteLayout = buildLayout("\"${quote.quote}\"", quotePaint, textWidth)
        }

        val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.background.authorColor
            typeface = typefaceFor(context, "playfair_display")
            textSize = width * 0.042f
        }
        val authorLayout = buildLayout("~ ${quote.author}", authorPaint, textWidth)

        // Center the quote + author block vertically
        val gap = width * 0.05f
        val blockHeight = quoteLayout.height + gap + authorLayout.height
        val top = (height - blockHeight) / 2f

        canvas.withTranslation(horizontalPadding, top) {
            quoteLayout.draw(this)
            translate(0f, quoteLayout.height + gap)
            authorLayout.draw(this)
        }

        // FlipQuotes watermark
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.background.authorColor
            alpha = 170
            typeface = typefaceFor(context, "playfair_display")
            textSize = width * 0.032f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("FlipQuotes", width / 2f, height - height * 0.05f, brandPaint)

        return bitmap
    }

    private fun buildLayout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .build()

    private fun drawBackground(context: Context, canvas: Canvas, background: ShareBackground, width: Int, height: Int) {
        val colors = background.colors
        when {
            colors == null -> {
                val texture = try {
                    BitmapFactory.decodeResource(context.resources, R.drawable.texture)
                } catch (_: Exception) {
                    null
                }
                if (texture != null) {
                    canvas.drawBitmap(texture, centerCropRect(texture, width, height), Rect(0, 0, width, height), Paint(Paint.FILTER_BITMAP_FLAG))
                } else {
                    canvas.drawColor(0xFFF5F5DC.toInt())
                }
            }
            colors.size == 1 -> canvas.drawColor(colors[0])
            else -> {
                val paint = Paint().apply {
                    shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), colors, null, Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }
    }

    // Source rect that crops the texture to the target aspect ratio without stretching
    private fun centerCropRect(source: Bitmap, width: Int, height: Int): Rect {
        val targetRatio = width.toFloat() / height
        val sourceRatio = source.width.toFloat() / source.height
        return if (sourceRatio > targetRatio) {
            val cropWidth = (source.height * targetRatio).toInt()
            val left = (source.width - cropWidth) / 2
            Rect(left, 0, left + cropWidth, source.height)
        } else {
            val cropHeight = (source.width / targetRatio).toInt()
            val top = (source.height - cropHeight) / 2
            Rect(0, top, source.width, top + cropHeight)
        }
    }

    // Mirrors the font names used by ThemeManager / QuoteContent
    private fun typefaceFor(context: Context, fontName: String): Typeface {
        fun resFont(id: Int) = try {
            ResourcesCompat.getFont(context, id) ?: Typeface.DEFAULT
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
        return when (fontName) {
            "kotta_one" -> resFont(R.font.kotta_one)
            "playfair_display" -> resFont(R.font.playfair_display)
            "droid_sans" -> resFont(R.font.droid_sans)
            "default" -> Typeface.DEFAULT
            "sans_serif", "fantasy" -> Typeface.SANS_SERIF
            "serif" -> Typeface.SERIF
            "monospace" -> Typeface.MONOSPACE
            "cursive" -> Typeface.create("cursive", Typeface.NORMAL)
            else -> resFont(R.font.kotta_one)
        }
    }
}
