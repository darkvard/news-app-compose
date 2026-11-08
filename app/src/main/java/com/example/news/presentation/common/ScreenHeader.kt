package com.example.news.presentation.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.news.ui.adaptive.rememberWindowLayout
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing

object ScreenHeaderDefaults {
    private val Height = 64.dp

    /** Short windows (phone in landscape) need every dp of height for content. */
    private val CompactHeight = 52.dp
    val EndPadding = 8.dp

    /** Public: the bookmark selection bar uses the same height so swapping them doesn't jump. */
    @Composable
    fun height(): Dp = if (rememberWindowLayout().isCompactHeight) CompactHeight else Height

    @Composable
    fun titleStyle(): TextStyle =
        if (rememberWindowLayout().isCompactHeight) MaterialTheme.typography.headlineSmall
        else MaterialTheme.typography.headlineMedium
}

/** Title row shared by the three tabs (News / Search / Bookmark) so they look consistent. */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ScreenHeaderDefaults.height())
            .padding(start = Spacing.ScreenHorizontal, end = ScreenHeaderDefaults.EndPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = ScreenHeaderDefaults.titleStyle(),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
@Preview(showBackground = true)
private fun ScreenHeaderPreview() {
    NewsTheme { ScreenHeader(title = "News") }
}
