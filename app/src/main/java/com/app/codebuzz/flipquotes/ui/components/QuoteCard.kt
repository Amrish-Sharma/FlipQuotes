package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.codebuzz.flipquotes.data.Quote
import kotlinx.coroutines.launch

@Composable
fun QuoteCard(
    quote: Quote,
    swipeDirection: Int = 0, // 1 for next, -1 for previous
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    var isAnimating by remember { mutableStateOf(false) }

    // Animate card entry based on swipeDirection
    LaunchedEffect(swipeDirection, quote) {
        if (swipeDirection == 1) {
            // Coming from bottom
            offsetY.snapTo(1000f)
            alpha.snapTo(0f)
            offsetY.animateTo(0f, animationSpec = tween(300))
            alpha.animateTo(1f, animationSpec = tween(200))
        } else if (swipeDirection == -1) {
            // Coming from top
            offsetY.snapTo(-1000f)
            alpha.snapTo(0f)
            offsetY.animateTo(0f, animationSpec = tween(300))
            alpha.animateTo(1f, animationSpec = tween(200))
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .graphicsLayer {
                translationY = offsetY.value
                this.alpha = alpha.value
            }
            .pointerInput(isAnimating) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        when {
                            offsetY.value < -100f && !isAnimating -> {
                                isAnimating = true
                                scope.launch {
                                    offsetY.animateTo(-1000f, animationSpec = tween(300))
                                    alpha.animateTo(0f, animationSpec = tween(200))
                                    offsetY.snapTo(1000f)
                                    alpha.snapTo(0f)
                                    onNext()
                                    offsetY.animateTo(0f, animationSpec = tween(300))
                                    alpha.animateTo(1f, animationSpec = tween(200))
                                    isAnimating = false
                                }
                            }
                            offsetY.value > 100f && !isAnimating -> {
                                isAnimating = true
                                scope.launch {
                                    offsetY.animateTo(1000f, animationSpec = tween(300))
                                    alpha.animateTo(0f, animationSpec = tween(200))
                                    offsetY.snapTo(-1000f)
                                    alpha.snapTo(0f)
                                    onPrevious()
                                    offsetY.animateTo(0f, animationSpec = tween(300))
                                    alpha.animateTo(1f, animationSpec = tween(200))
                                    isAnimating = false
                                }
                            }
                            else -> {
                                scope.launch {
                                    offsetY.animateTo(0f, animationSpec = tween(300))
                                }
                            }
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (!isAnimating) {
                            scope.launch {
                                offsetY.snapTo(offsetY.value + dragAmount)
                            }
                        }
                    }
                )
            },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Box(modifier = Modifier.fillMaxWidth()) {
                    header()
                }
                // Main content
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Texture background only for quote area
                    Image(
                        painter = painterResource(id = com.app.codebuzz.flipquotes.R.drawable.texture),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                    QuoteContent(quote = quote)
                }
                // Footer
                Box(modifier = Modifier.fillMaxWidth()) {
                    footer()
                }
            }
        }
    }
}

@Composable
fun QuoteContent(quote: Quote, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Text(
            text = "\"${quote.quote}\"",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "~ ${quote.author}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}