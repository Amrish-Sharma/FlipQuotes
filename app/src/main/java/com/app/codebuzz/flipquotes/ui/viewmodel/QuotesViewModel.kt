package com.app.codebuzz.flipquotes.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.codebuzz.flipquotes.data.BookmarksRepository
import com.app.codebuzz.flipquotes.data.DailyQuoteProvider
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.data.QuotesRepository
import com.app.codebuzz.flipquotes.data.key
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class QuotesUiState { Loading, Ready, Error }

class QuotesViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val ALL_THEME = "All"
    }

    private val repository = QuotesRepository(application)

    private val _allQuotes = MutableStateFlow<List<Quote>>(emptyList())
    val allQuotes: StateFlow<List<Quote>>  get()= _allQuotes

    private val _themesList = MutableStateFlow<List<String>>(emptyList())
    val themesList: StateFlow<List<String>> = _themesList

    // Quotes per theme tab, shuffled once per load so the order survives tab switches and rotation
    private val _quotesByTheme = MutableStateFlow<Map<String, List<Quote>>>(emptyMap())
    val quotesByTheme: StateFlow<Map<String, List<Quote>>> = _quotesByTheme

    private val _uiState = MutableStateFlow(QuotesUiState.Loading)
    val uiState: StateFlow<QuotesUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // Emits true when a manual refresh brought new data, false when it failed
    private val _refreshResults = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val refreshResults: SharedFlow<Boolean> = _refreshResults

    private val bookmarksRepository = BookmarksRepository(application)
    private val _bookmarkedKeys = MutableStateFlow(bookmarksRepository.getAll())
    val bookmarkedKeys: StateFlow<Set<String>> = _bookmarkedKeys

    private val appContext = application.applicationContext
    private val _dailyQuote = MutableStateFlow<Quote?>(null)
    val dailyQuote: StateFlow<Quote?> = _dailyQuote

    init {
        fetchQuotes()
    }

    fun toggleBookmark(quote: Quote) {
        _bookmarkedKeys.value = bookmarksRepository.toggle(quote.key)
    }

    fun loadDailyQuote() {
        viewModelScope.launch {
            _dailyQuote.value = DailyQuoteProvider.getToday(appContext)
        }
    }

    // True the first time the app is opened on a given day
    fun shouldAutoShowDailyQuote(): Boolean = DailyQuoteProvider.shouldAutoShowToday(appContext)

    private fun publishQuotes(quotes: List<Quote>) {
        val themeFrequency = quotes
            .groupBy { it.theme }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(10) // Changed from 5 to 10
            .map { it.key }
        val themes = listOf(ALL_THEME) + themeFrequency // Removed "General"

        _allQuotes.value = quotes
        _quotesByTheme.value = themes.associateWith { theme ->
            when (theme) {
                ALL_THEME -> quotes.shuffled()
                else -> quotes.filter { quote -> quote.theme == theme }.shuffled()
            }
        }
        _themesList.value = themes
    }

    fun fetchQuotes() {
        viewModelScope.launch {
            _uiState.value = QuotesUiState.Loading
            val quotes = try {
                // Use the repository with caching - much faster!
                repository.getQuotes()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            if (quotes.isNotEmpty()) {
                publishQuotes(quotes)
                _uiState.value = QuotesUiState.Ready
            } else {
                _uiState.value = QuotesUiState.Error
            }
        }
    }

    // Force refresh for refresh button - bypasses cache
    fun forceRefresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            val quotes = try {
                // Force network refresh
                repository.forceRefresh()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            if (quotes.isNotEmpty()) {
                publishQuotes(quotes)
                _uiState.value = QuotesUiState.Ready
            }
            _isRefreshing.value = false
            _refreshResults.emit(quotes.isNotEmpty())
        }
    }
}
