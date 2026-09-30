package com.app.codebuzz.flipquotes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import com.app.codebuzz.flipquotes.ui.screens.QuotePagerScreen
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesViewModel
import com.app.codebuzz.flipquotes.ui.theme.FlipQuotesTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_DAILY_QUOTE = "open_daily_quote"
    }

    // Set when launched from the daily notification or the widget
    private val openDailyQuoteRequest = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openDailyQuoteRequest.value = intent.getBooleanExtra(EXTRA_OPEN_DAILY_QUOTE, false)
        val quotesViewModel = QuotesViewModel(this)
        setContent {
            FlipQuotesTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    QuotePagerScreen(
                        viewModel = quotesViewModel,
                        openDailyQuoteRequest = openDailyQuoteRequest.value,
                        onDailyQuoteRequestHandled = { openDailyQuoteRequest.value = false }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_DAILY_QUOTE, false)) {
            openDailyQuoteRequest.value = true
        }
    }
}
