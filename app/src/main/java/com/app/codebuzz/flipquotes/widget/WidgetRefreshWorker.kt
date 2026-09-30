package com.app.codebuzz.flipquotes.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.codebuzz.flipquotes.data.DailyQuoteProvider

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daily_quote_widget_refresh"
    }

    override suspend fun doWork(): Result {
        DailyQuoteProvider.getToday(applicationContext) ?: return Result.retry()
        DailyQuoteWidget().updateAll(applicationContext)
        return Result.success()
    }
}
