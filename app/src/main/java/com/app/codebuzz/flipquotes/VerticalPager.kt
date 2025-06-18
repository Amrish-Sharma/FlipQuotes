import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.app.codebuzz.flipquotes.data.Quote
import com.app.codebuzz.flipquotes.ui.screens.CardLayout

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlipQuotesPager(quotes: List<Quote>) {
    val pagerState = rememberPagerState { quotes.size }

    VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        CardLayout(quote = quotes[page])
    }
}
