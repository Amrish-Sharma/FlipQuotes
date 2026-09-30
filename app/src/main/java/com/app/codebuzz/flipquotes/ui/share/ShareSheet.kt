package com.app.codebuzz.flipquotes.ui.share

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.codebuzz.flipquotes.R
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ShareSheet(
    quote: Quote,
    quoteFont: String,
    theme: AppTheme,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var style by remember { mutableStateOf(ShareStyle.load(context)) }

    // Render off the main thread; the same bitmap is used for preview, share and save
    val bitmap by produceState<Bitmap?>(initialValue = null, quote, style, quoteFont) {
        value = withContext(Dispatchers.Default) {
            QuoteImageRenderer.render(context, quote, style, quoteFont)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.surfaceColor,
        contentColor = theme.onSurfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Share Quote", fontWeight = FontWeight.Bold)

            // Live preview
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .aspectRatio(style.format.width.toFloat() / style.format.height)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, theme.onSurfaceColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val current = bitmap
                if (current != null) {
                    Image(
                        bitmap = current.asImageBitmap(),
                        contentDescription = "Share preview",
                        contentScale = ContentScale.Fit
                    )
                } else {
                    CircularProgressIndicator(color = theme.onSurfaceColor)
                }
            }

            // Background swatches
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(ShareBackground.entries) { background ->
                    BackgroundSwatch(
                        background = background,
                        selected = background == style.background,
                        theme = theme,
                        onClick = { style = style.copy(background = background) }
                    )
                }
            }

            // Format picker
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ShareFormat.entries.forEachIndexed { index, format ->
                    SegmentedButton(
                        selected = format == style.format,
                        onClick = { style = style.copy(format = format) },
                        shape = SegmentedButtonDefaults.itemShape(index, ShareFormat.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = theme.onSurfaceColor.copy(alpha = 0.15f),
                            activeContentColor = theme.onSurfaceColor,
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = theme.onSurfaceColor.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(format.label)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        bitmap?.let {
                            style.save(context)
                            val saved = saveQuoteImageToGallery(context, it, quote) != null
                            Toast.makeText(
                                context,
                                if (saved) "Saved to Pictures/FlipQuotes" else "Couldn't save image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = bitmap != null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.onSurfaceColor)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Save")
                }
                Button(
                    onClick = {
                        bitmap?.let {
                            style.save(context)
                            shareQuoteImage(context, quote, it)
                            onDismiss()
                        }
                    },
                    enabled = bitmap != null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.onSurfaceColor,
                        contentColor = theme.surfaceColor
                    )
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Share")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BackgroundSwatch(
    background: ShareBackground,
    selected: Boolean,
    theme: AppTheme,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val swatchModifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) theme.onSurfaceColor else theme.onSurfaceColor.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
        val colors = background.colors
        when {
            colors == null -> Image(
                painter = painterResource(R.drawable.texture),
                contentDescription = background.label,
                contentScale = ContentScale.Crop,
                modifier = swatchModifier
            )
            colors.size == 1 -> Box(swatchModifier.background(Color(colors[0])))
            else -> Box(swatchModifier.background(Brush.linearGradient(colors.map { Color(it) })))
        }
        Text(
            text = background.label,
            color = theme.onSurfaceColor.copy(alpha = if (selected) 1f else 0.6f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
