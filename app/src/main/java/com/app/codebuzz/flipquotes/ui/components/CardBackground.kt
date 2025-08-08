package com.app.codebuzz.flipquotes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.app.codebuzz.flipquotes.R

@Composable
fun CardBackground(
    backgroundType: String,
    modifier: Modifier = Modifier
) {
    when (backgroundType) {
        "white" -> {
            // Classic White - Elegant white background with subtle ornamental patterns
            Image(
                painter = painterResource(id = R.drawable.bg_classic),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }

        "gradient_sunset" -> {
            // Gradient Sunset - Warm sunset gradient background
            Image(
                painter = painterResource(id = R.drawable.bg_sunset),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }

        "minimalist" -> {
            // Minimalist - Clean modern background with geometric patterns
            Image(
                painter = painterResource(id = R.drawable.bg_minimalist),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }

        "nature_pattern" -> {
            // Nature Pattern - Organic green background with leaf patterns
            Image(
                painter = painterResource(id = R.drawable.bg_nature),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }

        else -> {
            // Default fallback to classic
            Image(
                painter = painterResource(id = R.drawable.bg_classic),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
