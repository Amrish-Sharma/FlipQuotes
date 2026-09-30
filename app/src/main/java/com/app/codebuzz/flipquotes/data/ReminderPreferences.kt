package com.app.codebuzz.flipquotes.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class ReminderPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean("enabled", false)
        set(value) = prefs.edit { putBoolean("enabled", value) }

    var hour: Int
        get() = prefs.getInt("hour", 8)
        set(value) = prefs.edit { putInt("hour", value) }

    var minute: Int
        get() = prefs.getInt("minute", 0)
        set(value) = prefs.edit { putInt("minute", value) }
}
