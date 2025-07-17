package com.app.codebuzz.flipquotes.data

import android.content.Context
import android.content.SharedPreferences
import com.app.codebuzz.flipquotes.data.Quote
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import androidx.core.content.edit

class QuotesRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("quotes_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val CACHE_FILE_NAME = "quotes_cache.json"
        private const val LAST_UPDATE_KEY = "last_update_timestamp"
        private const val CACHE_EXPIRY_HOURS = 24 // Cache expires after 24 hours
        private const val QUOTES_URL = "https://raw.githubusercontent.com/Amrish-Sharma/fq_quotes/refs/heads/quote-with-theme/quote_with_theme.json"
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
                gson.fromJson(json, listType) ?: emptyList()
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
                val json = response.body?.string() ?: ""
                if (json.isNotEmpty()) {
                    val listType = object : TypeToken<List<Quote>>() {}.type
                    gson.fromJson(json, listType) ?: emptyList()
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

    // Get cache info for debugging
    fun getCacheInfo(): String {
        val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
        val lastUpdate = prefs.getLong(LAST_UPDATE_KEY, 0)
        val cacheAge = System.currentTimeMillis() - lastUpdate

        return "Cache exists: ${cacheFile.exists()}, " +
               "Size: ${if (cacheFile.exists()) "${cacheFile.length()} bytes" else "0"}, " +
               "Age: ${TimeUnit.MILLISECONDS.toMinutes(cacheAge)} minutes"
    }
}
