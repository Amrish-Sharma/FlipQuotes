package com.app.codebuzz.flipquotes.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.data.QuotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuotesViewModel(context: Context) : ViewModel() {
    private val repository = QuotesRepository(context)

    private val _allQuotes = MutableStateFlow<List<Quote>>(emptyList())
    val allQuotes: StateFlow<List<Quote>>  get()= _allQuotes
    private val _filteredQuotes = MutableStateFlow<List<Quote>>(emptyList())
    val filteredQuotes: StateFlow<List<Quote>> = _filteredQuotes

    private val _themesList = MutableStateFlow<List<String>>(emptyList())
    val themesList: StateFlow<List<String>> = _themesList

    private val _selectedTheme = MutableStateFlow<String?>(null)
    val selectedTheme: StateFlow<String?> = _selectedTheme

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchQuotes()
    }

    fun setSelectedTheme(theme: String?) {
        _selectedTheme.value = theme
        filterQuotesByTheme()
    }

    private fun processThemes(quotes: List<Quote>) {
        val themeFrequency = quotes
            .groupBy { it.theme }
            .mapValues { it.value.size }
            .entries
            .sortedByDescending { it.value }
            .take(10) // Changed from 5 to 10
            .map { it.key }

        _themesList.value = listOf("All") + themeFrequency // Removed "General"
    }

    private fun filterQuotesByTheme() {
        val theme = _selectedTheme.value
        _filteredQuotes.update { currentQuotes ->
            when {
                theme == null || theme == "All" -> _allQuotes.value.shuffled()
                else -> _allQuotes.value
                    .filter { quote -> quote.theme == theme }
                    .shuffled()
            }
        }
    }

    fun fetchQuotes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Use the repository with caching - much faster!
                val quotes = repository.getQuotes()

                if (quotes.isNotEmpty()) {
                    _allQuotes.value = quotes
                    processThemes(quotes)
                    filterQuotesByTheme()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error state if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Force refresh for refresh button - bypasses cache
    fun forceRefresh() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Force network refresh
                val quotes = repository.forceRefresh()

                if (quotes.isNotEmpty()) {
                    _allQuotes.value = quotes
                    processThemes(quotes)
                    filterQuotesByTheme()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
