package com.example.news.presentation.navigator.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.news.R
import com.example.news.presentation.navgraph.Destination

data class BottomNavigationItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    /** Not displayed (icon-only bar) but used as the accessibility label. */
    @StringRes val label: Int,
    val destination: Destination
)

/** The app's three tabs, in display order. */
val NewsBottomNavigationItems = listOf(
    BottomNavigationItem(Icons.Rounded.Home, Icons.Outlined.Home, R.string.tab_news, Destination.Home),
    BottomNavigationItem(Icons.Rounded.Search, Icons.Outlined.Search, R.string.search, Destination.Search),
    BottomNavigationItem(Icons.Rounded.Bookmark, Icons.Outlined.BookmarkBorder, R.string.bookmark, Destination.Bookmark),
)
