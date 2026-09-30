package com.app.codebuzz.flipquotes.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.app.codebuzz.flipquotes.notifications.ReminderScheduler
import java.util.concurrent.TimeUnit

class DailyQuoteWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = DailyQuoteWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Refresh just after midnight so the widget rolls over to the new day's quote
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(ReminderScheduler.delayUntil(0, 5).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WidgetRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context).cancelUniqueWork(WidgetRefreshWorker.WORK_NAME)
    }
}
