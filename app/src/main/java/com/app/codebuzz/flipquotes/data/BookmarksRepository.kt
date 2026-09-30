package com.app.codebuzz.flipquotes.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class BookmarksRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE)

    companion object {
        private const val BOOKMARKS_KEY = "bookmarked_keys"
    }

    fun getAll(): Set<String> = prefs.getStringSet(BOOKMARKS_KEY, emptySet())?.toSet() ?: emptySet()

    fun isBookmarked(key: String): Boolean = key in getAll()

    // Returns the updated set of bookmarked keys
    fun toggle(key: String): Set<String> {
        val current = getAll()
        val updated = if (key in current) current - key else current + key
        prefs.edit { putStringSet(BOOKMARKS_KEY, updated) }
        return updated
    }
}