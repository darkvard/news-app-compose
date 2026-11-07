package com.example.news.presentation.navigator.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

private object BottomBarDefaults {
    val Height = 60.dp
    val InnerPadding = 6.dp
    val IconSize = 26.dp
    val BorderWidth = 1.dp
    const val BorderAlpha = 0.5f
    val ShadowElevation = 12.dp
    const val ShadowAlpha = 0.2f
}

/**
 * Floating, icon-only bottom bar. Its background is a "frosted glass" blur of whatever scrolls
 * behind it: the content must be marked with `Modifier.hazeSource(hazeState)`.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun NewsBottomNavigation(
    items: List<BottomNavigationItem>,
    selectedItem: Int,
    onItemClick: (Int) -> Unit,
    hazeState: HazeState?,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(BottomBarDefaults.Height)
            .shadow(
                elevation = BottomBarDefaults.ShadowElevation,
                shape = CircleShape,
                ambientColor = colors.primary.copy(alpha = BottomBarDefaults.ShadowAlpha),
                spotColor = colors.primary.copy(alpha = BottomBarDefaults.ShadowAlpha)
            )
            .clip(CircleShape)
            .hazeEffect(state = hazeState, style = HazeMaterials.thin(colors.surfaceContainerLowest))
            .border(
                width = BottomBarDefaults.BorderWidth,
                color = colors.outlineVariant.copy(alpha = BottomBarDefaults.BorderAlpha),
                shape = CircleShape
            )
            .padding(BottomBarDefaults.InnerPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            BottomBarItem(
                item = item,
                selected = index == selectedItem,
                onClick = { onItemClick(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    item: BottomNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) colors.primaryContainer else Color.Transparent,
        label = "bottomBarIndicator"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.onSurfaceVariant,
        label = "bottomBarIcon"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(indicatorColor)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = stringResource(item.label),
            tint = iconColor,
            modifier = Modifier.size(BottomBarDefaults.IconSize)
        )
    }
}

@Composable
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun NewsBottomNavigationPreview() {
    NewsTheme {
        Box(modifier = Modifier.padding(Spacing.ExtraLarge)) {
            NewsBottomNavigation(
                items = NewsBottomNavigationItems,
                selectedItem = 0,
                onItemClick = {},
                hazeState = null
            )
        }
    }
}
