package com.app.codebuzz.flipquotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.codebuzz.flipquotes.data.Quote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class QuotesViewModel : ViewModel() {
    private val _quotes = MutableStateFlow<List<Quote>>(emptyList())
    val quotes: StateFlow<List<Quote>> = _quotes

    init {
        fetchQuotes()
    }

    private fun fetchQuotes() {
        viewModelScope.launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://raw.githubusercontent.com/Amrish-Sharma/fq_quotes/refs/heads/quote-with-theme/quote_with_theme.json")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val listType = object : TypeToken<List<Quote>>() {}.type
                    val quotesList: List<Quote> = Gson().fromJson(json, listType)
                    _quotes.value = quotesList.shuffled()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
