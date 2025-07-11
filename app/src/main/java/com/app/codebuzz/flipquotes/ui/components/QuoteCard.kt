package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.codebuzz.flipquotes.data.Quote
import kotlinx.coroutines.launch
import com.app.codebuzz.flipquotes.R
import kotlin.math.abs

@Composable
fun QuoteCard(
    quote: Quote,
    nextQuote: Quote,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    remember { Animatable(1f) }
    val scale = remember { Animatable(1f) }
    var isAnimating by remember { mutableStateOf(false) }

    // Faster animation for smoother transitions
    val tweenSpec = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    // Snappier spring for better responsiveness
    val springSpec = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = 200f
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Next card (rendered underneath)
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val progress = (-offsetY.value).coerceIn(0f, 1000f) / 1000f
                    scaleX = 0.9f + (0.1f * progress)
                    scaleY = 0.9f + (0.1f * progress)
                    translationY = 40f * (1f - progress)
                    this.alpha = (progress * 1.3f).coerceIn(0f, 1f)
                    rotationX = -35f * (1f - progress)
                    cameraDistance = 8f
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Texture background covers the entire card
                Image(
                    painter = painterResource(id = R.drawable.texture),
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                    ) {
                        header()
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "\"${nextQuote.quote}\"",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily(Font(resId = R.font.kotta_one))
                                ),
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "~ ${nextQuote.author}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        footer()
                    }
                }
            }
        }

        // Current card (on top)
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = offsetY.value
                    val progress = (-offsetY.value).coerceIn(0f, 1000f) / 1000f
                    rotationX = 35f * progress
                    scaleX = scale.value * (1f - 0.1f * progress)
                    scaleY = scale.value * (1f - 0.1f * progress)
                    this.alpha = 1f - (progress * 0.8f)
                    cameraDistance = 8f
                }
                .pointerInput(isAnimating) {
                    detectVerticalDragGestures(
                        onDragStart = { _ ->
                            scope.launch {
                                scale.animateTo(0.98f,
                                    spring(
                                        dampingRatio = 0.75f,
                                        stiffness = 300f
                                    )
                                )
                            }
                        },
                        onDragEnd = {
                            when {
                                offsetY.value < -80f && !isAnimating -> {
                                    isAnimating = true
                                    scope.launch {
                                        try {
                                            offsetY.animateTo(-1000f, tweenSpec)
                                            onNext()
                                            offsetY.snapTo(0f)
                                            scale.snapTo(1f)
                                        } finally {
                                            isAnimating = false
                                        }
                                    }
                                }
                                offsetY.value > 80f && !isAnimating -> {
                                    isAnimating = true
                                    scope.launch {
                                        try {
                                            offsetY.animateTo(1000f, tweenSpec)
                                            onPrevious()
                                            offsetY.snapTo(0f)
                                            scale.snapTo(1f)
                                        } finally {
                                            isAnimating = false
                                        }
                                    }
                                }
                                else -> {
                                    scope.launch {
                                        offsetY.animateTo(0f, springSpec)
                                        scale.animateTo(1f, springSpec)
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetY.animateTo(0f, springSpec)
                                scale.animateTo(1f, springSpec)
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            scope.launch {
                                val resistance = 0.5f + (abs(offsetY.value) / 1000f) * 0.5f
                                val targetOffset = (offsetY.value + dragAmount / resistance)
                                    .coerceIn(-1000f, 1000f)
                                offsetY.snapTo(targetOffset)
                            }
                        }
                    )
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Texture background covers the entire card
                Image(
                    painter = painterResource(id = R.drawable.texture),
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header with status bar padding
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                    ) {
                        header()
                    }
                    // Main content (no system bar padding)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "\"${quote.quote}\"",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily(Font(resId = R.font.kotta_one))
                                ),
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "~ ${quote.author}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    // Footer with navigation bar padding
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        footer()
                    }
                }
            }
        }
    }
}

@Composable
fun ShareableQuoteCard(quote: Quote) {
    // Draw the texture background with rectangular corners, no black or solid color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Set black background for padding/edges
    ) {
        Card(
            shape = RectangleShape, // Rectangular corners
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.texture),
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = quote.quote,
                            style = MaterialTheme.typography.headlineLarge
                            .copy(fontFamily = FontFamily(Font(resId = R.font.kotta_one))),
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "- ${quote.author}",
                            style = MaterialTheme.typography.bodyLarge
                            .copy(fontFamily = FontFamily(Font(resId = R.font.kotta_one))),
                            color = Color.DarkGray,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
