package com.app.codebuzz.flipquotes.notifications

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.codebuzz.flipquotes.data.DailyQuoteProvider
import com.app.codebuzz.flipquotes.widget.DailyQuoteWidget

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val quote = DailyQuoteProvider.getToday(applicationContext) ?: return Result.retry()
        NotificationHelper.showDailyQuote(applicationContext, quote)
        DailyQuoteWidget().updateAll(applicationContext)
        return Result.success()
    }
}
