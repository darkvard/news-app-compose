package com.example.news.presentation.navigator.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing
import dev.chrisbanes.haze.HazeState

/**
 * Vertical version of [NewsBottomNavigation] for short or wide windows (phone in landscape, tablet):
 * same floating pill, same items, stacked top to bottom. Only as tall as its items.
 */
@Composable
fun NewsNavigationRail(
    items: List<BottomNavigationItem>,
    selectedItem: Int,
    onItemClick: (Int) -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(FloatingNavDefaults.Thickness)
            .floatingNavSurface(hazeState),
        verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { index, item ->
            FloatingNavItem(
                item = item,
                selected = index == selectedItem,
                onClick = { onItemClick(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FloatingNavDefaults.ItemLength)
            )
        }
    }
}

@Composable
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun NewsNavigationRailPreview() {
    NewsTheme {
        Box(modifier = Modifier.padding(Spacing.ExtraLarge)) {
            NewsNavigationRail(
                items = NewsBottomNavigationItems,
                selectedItem = 1,
                onItemClick = {},
                hazeState = null
            )
        }
    }
}
