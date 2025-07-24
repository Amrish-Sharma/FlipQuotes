package com.app.codebuzz.flipquotes.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class QuotesRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("quotes_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val CACHE_FILE_NAME = "quotes_cache.json"
        private const val LAST_UPDATE_KEY = "last_update_timestamp"
        private const val CACHE_EXPIRY_HOURS = 24 // Cache expires after 24 hours
        private const val QUOTES_URL = "https://raw.githubusercontent.com/Amrish-Sharma/fq_quotes/refs/heads/quote-with-theme/quote_with_theme.json"
    }

    private fun generateFlippedQuote(originalQuote: String): String {
        val flippingSuffixes = listOf(
            "...or so they say",
            "...said no one ever",
            "...in your dreams",
            "...yeah right",
            "...if only it were that simple",
            "...easier said than done",
            "...sure, let me get right on that",
            "...welcome to reality",
            "...in a perfect world maybe",
            "...that's adorable"
        )
        
        val sarcasticPrefixes = listOf(
            "Obviously, ",
            "Clearly, ",
            "Of course, ",
            "Naturally, ",
            "Apparently, "
        )
        
        val flipPatterns = listOf(
            // Add sarcastic suffix
            { quote: String -> "$quote ${flippingSuffixes.random()}" },
            // Add sarcastic prefix  
            { quote: String -> "${sarcasticPrefixes.random()}$quote" },
            // Replace positive words with negative
            { quote: String -> 
                quote.replace("success", "failure", ignoreCase = true)
                     .replace("always", "never", ignoreCase = true)
                     .replace("possible", "impossible", ignoreCase = true)
                     .replace("can", "cannot", ignoreCase = true)
                     .replace("will", "will not", ignoreCase = true)
                     .replace("yes", "no", ignoreCase = true)
            },
            // Add ironic twist
            { quote: String -> "The opposite of '$quote' is probably true" }
        )
        
        return flipPatterns.random().invoke(originalQuote)
    }

    suspend fun getQuotes(): List<Quote> = withContext(Dispatchers.IO) {
        try {
            // Check if cache is valid and exists
            if (isCacheValid() && doesCacheExist()) {
                // Load from local cache - Super fast!
                val cachedQuotes = loadFromCache()
                if (cachedQuotes.isNotEmpty()) {
                    return@withContext cachedQuotes
                }
            }

            // Cache is invalid or doesn't exist, fetch from network
            val networkQuotes = fetchFromNetwork()
            if (networkQuotes.isNotEmpty()) {
                // Save to cache for future use
                saveToCache(networkQuotes)
                return@withContext networkQuotes
            }

            // Network failed, try to load any existing cache as fallback
            loadFromCache()

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to cache if network fails
            loadFromCache()
        }
    }

    private fun isCacheValid(): Boolean {
        val lastUpdate = prefs.getLong(LAST_UPDATE_KEY, 0)
        val currentTime = System.currentTimeMillis()
        val cacheAge = currentTime - lastUpdate
        val maxAge = TimeUnit.HOURS.toMillis(CACHE_EXPIRY_HOURS.toLong())

        return cacheAge < maxAge
    }

    private fun doesCacheExist(): Boolean {
        val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
        return cacheFile.exists() && cacheFile.length() > 0
    }

    private fun loadFromCache(): List<Quote> {
        return try {
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            if (cacheFile.exists()) {
                val json = cacheFile.readText()
                val listType = object : TypeToken<List<Quote>>() {}.type
                val quotes: List<Quote> = gson.fromJson(json, listType) ?: emptyList()
                // Add flipped quotes if they don't exist
                quotes.map { quote ->
                    if (quote.flippedQuote == null) {
                        quote.copy(flippedQuote = generateFlippedQuote(quote.quote))
                    } else {
                        quote
                    }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchFromNetwork(): List<Quote> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(QUOTES_URL)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    val json = body.string()
                    if (json.isNotEmpty()) {
                        val listType = object : TypeToken<List<Quote>>() {}.type
                        val quotes: List<Quote> = gson.fromJson(json, listType) ?: emptyList()
                        // Add flipped quotes to network data
                        quotes.map { quote ->
                            quote.copy(flippedQuote = generateFlippedQuote(quote.quote))
                        }
                    } else {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun saveToCache(quotes: List<Quote>) {
        try {
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            val json = gson.toJson(quotes)
            cacheFile.writeText(json)

            // Update last cache timestamp
            prefs.edit {
                putLong(LAST_UPDATE_KEY, System.currentTimeMillis())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Force refresh from network (for refresh button)
    suspend fun forceRefresh(): List<Quote> = withContext(Dispatchers.IO) {
        val networkQuotes = fetchFromNetwork()
        if (networkQuotes.isNotEmpty()) {
            saveToCache(networkQuotes)
        }
        networkQuotes.ifEmpty { loadFromCache() }
    }

}
