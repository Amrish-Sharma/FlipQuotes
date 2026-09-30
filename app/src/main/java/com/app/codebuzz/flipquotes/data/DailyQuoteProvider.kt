package com.app.codebuzz.flipquotes.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import java.time.LocalDate
import kotlin.random.Random

object DailyQuoteProvider {

    private const val PREFS_NAME = "daily_quote"
    private const val DATE_KEY = "daily_quote_date"
    private const val QUOTE_KEY = "daily_quote_json"
    private const val LAST_SHOWN_KEY = "daily_quote_last_shown"

    private val gson = Gson()

    // Deterministic pick so the same date always maps to the same quote
    fun pickForDate(quotes: List<Quote>, date: LocalDate): Quote? {
        if (quotes.isEmpty()) return null
        return quotes[Random(date.toEpochDay()).nextInt(quotes.size)]
    }

    // Today's quote, pinned in prefs so it stays stable even if the cache refreshes mid-day
    suspend fun getToday(context: Context): Quote? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()

        if (prefs.getString(DATE_KEY, null) == today) {
            prefs.getString(QUOTE_KEY, null)?.let { json ->
                runCatching { gson.fromJson(json, Quote::class.java) }.getOrNull()?.let { return it }
            }
        }

        val quote = pickForDate(QuotesRepository(context).getQuotes(), LocalDate.now()) ?: return null
        prefs.edit {
            putString(DATE_KEY, today)
            putString(QUOTE_KEY, gson.toJson(quote))
        }
        return quote
    }

    // True only the first time it's called on a given day
    fun shouldAutoShowToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()
        if (prefs.getString(LAST_SHOWN_KEY, null) == today) return false
        prefs.edit { putString(LAST_SHOWN_KEY, today) }
        return true
    }
}
