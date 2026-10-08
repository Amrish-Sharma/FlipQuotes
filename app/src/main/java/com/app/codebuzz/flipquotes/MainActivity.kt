package com.app.codebuzz.flipquotes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.app.codebuzz.flipquotes.ui.screens.QuotePagerScreen
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesUiState
import com.app.codebuzz.flipquotes.ui.viewmodel.QuotesViewModel
import com.app.codebuzz.flipquotes.ui.theme.FlipQuotesTheme
import com.app.codebuzz.flipquotes.ui.theme.rememberThemeManager

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_DAILY_QUOTE = "open_daily_quote"
        private const val SPLASH_MAX_HOLD_MS = 1500L
    }

    private val quotesViewModel: QuotesViewModel by viewModels()

    // Set when launched from the daily notification or the widget
    private val openDailyQuoteRequest = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            openDailyQuoteRequest.value = intent.getBooleanExtra(EXTRA_OPEN_DAILY_QUOTE, false)
        }
        setContent {
            val themeManager = rememberThemeManager()
            val theme by themeManager.currentTheme

            // Keep status and navigation bar icons readable on the in-app theme
            LaunchedEffect(theme.isDark) {
                val barStyle = if (theme.isDark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }

            FlipQuotesTheme(theme = theme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    QuotePagerScreen(
                        viewModel = quotesViewModel,
                        themeManager = themeManager,
                        openDailyQuoteRequest = openDailyQuoteRequest.value,
                        onDailyQuoteRequestHandled = { openDailyQuoteRequest.value = false }
                    )
                }
            }
        }
        keepSplashWhileQuotesLoad()
    }

    // Hold the system splash while cached quotes load; capped so a slow network never stalls launch
    private fun keepSplashWhileQuotesLoad() {
        val startedAt = SystemClock.elapsedRealtime()
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val loading = quotesViewModel.uiState.value == QuotesUiState.Loading
                if (loading && SystemClock.elapsedRealtime() - startedAt < SPLASH_MAX_HOLD_MS) {
                    return false
                }
                content.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_DAILY_QUOTE, false)) {
            openDailyQuoteRequest.value = true
        }
    }
}
