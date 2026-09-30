package com.app.codebuzz.flipquotes.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontStyle
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.app.codebuzz.flipquotes.MainActivity
import com.app.codebuzz.flipquotes.data.DailyQuoteProvider

class DailyQuoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val quote = DailyQuoteProvider.getToday(context)
        // Follow the app's saved theme (black is the app default)
        val isDark = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
            .getString("selected_theme", "black") == "black"

        val background = if (isDark) Color.Black else Color.White
        val textColor = if (isDark) Color.White else Color.Black
        val subtleColor = if (isDark) Color(0xFFBBBBBB) else Color(0xFF555555)

        val openDailyQuote = actionStartActivity<MainActivity>(
            actionParametersOf(ActionParameters.Key<Boolean>(MainActivity.EXTRA_OPEN_DAILY_QUOTE) to true)
        )

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(background)
                    .cornerRadius(16.dp)
                    .padding(16.dp)
                    .clickable(openDailyQuote),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "QUOTE OF THE DAY",
                    style = TextStyle(color = ColorProvider(subtleColor), fontSize = 11.sp)
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = quote?.let { "\"${it.quote}\"" } ?: "Open FlipQuotes to load today's quote",
                    maxLines = 6,
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier.fillMaxWidth()
                )
                if (quote != null) {
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        text = "~ ${quote.author}",
                        style = TextStyle(
                            color = ColorProvider(subtleColor),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        ),
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
