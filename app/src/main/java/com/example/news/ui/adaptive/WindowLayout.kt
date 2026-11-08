package com.example.news.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowSizeClass

/**
 * How much room the window gives us, derived from the window size class (not from "portrait vs
 * landscape"), so the same rules work for phones, foldables, tablets and split screen.
 *
 * - [isCompactHeight]: short window (a phone in landscape). Screens save vertical space:
 *   side-by-side panes, smaller headers.
 * - [useNavigationRail]: tabs go in a vertical rail on the left instead of the bottom bar
 *   (short windows, and wide ones like tablets).
 */
@Immutable
data class WindowLayout(
    val isCompactHeight: Boolean,
    val isExpandedWidth: Boolean
) {
    val useNavigationRail: Boolean get() = isCompactHeight || isExpandedWidth
}

@Composable
fun rememberWindowLayout(): WindowLayout {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(sizeClass) {
        WindowLayout(
            isCompactHeight = !sizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND),
            isExpandedWidth = sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
        )
    }
}

/** `@Preview(device = ...)` for a phone in landscape: use it to preview the compact-height layouts. */
const val LANDSCAPE_PHONE_PREVIEW = "spec:width=891dp,height=411dp"
