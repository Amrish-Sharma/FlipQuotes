package com.app.codebuzz.flipquotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.app.codebuzz.flipquotes.ui.screens.QuotePagerScreen
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesViewModel
import com.app.codebuzz.flipquotes.ui.theme.FlipQuotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val quotesViewModel = QuotesViewModel(this)
        setContent {
            FlipQuotesTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    QuotePagerScreen(quotesViewModel)
                }
            }
        }
    }
}