package com.app.codebuzz.flipquotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.app.codebuzz.flipquotes.ui.screens.QuotePagerScreen
import com.app.codebuzz.flipquotes.ui.screens.QuoteViewModel
import com.app.codebuzz.flipquotes.ui.theme.FlipQuotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val quoteViewModel: QuoteViewModel by viewModels()
        setContent {
            FlipQuotesTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    QuotePagerScreen(quoteViewModel)
                }
            }
        }
    }
}