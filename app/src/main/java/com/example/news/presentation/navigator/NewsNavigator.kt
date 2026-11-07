package com.example.news.presentation.navigator

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.news.domain.model.Article
import com.example.news.presentation.bookmark.BookmarkRoute
import com.example.news.presentation.common.OfflineBanner
import com.example.news.presentation.detail.DetailRoute
import com.example.news.presentation.home.HomeRoute
import com.example.news.presentation.navgraph.Destination
import com.example.news.presentation.navigator.components.NewsBottomNavigation
import com.example.news.presentation.navigator.components.NewsBottomNavigationItems
import com.example.news.presentation.search.SearchRoute
import com.example.news.ui.theme.Spacing
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private object NavigatorDefaults {
    val BarHorizontalMargin = 24.dp

    /** The bar overlaps this much of the (mostly empty) gesture-handle inset... */
    val NavBarInsetOverlap = 4.dp

    /** ...but always keeps at least this gap to the bottom edge. */
    val MinBottomMargin = 8.dp
}

/** Hosts the bottom bar and the tab destinations. Each destination is a `XxxRoute`. */
@Composable
fun NewsNavigator(
    viewModel: NavigatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val items = NewsBottomNavigationItems

    // Index of the tab whose destination is on screen (-1 = not a tab, e.g. Details)
    val selectedItem = items.indexOfFirst { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.destination::class) } == true
    }
    val navigateToDetails: (Article) -> Unit = { navController.navigate(Destination.Details(it)) }

    // Content behind the floating bar is the haze "source"; the bar blurs it
    val hazeState = rememberHazeState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            FloatingBottomBar(
                isOnline = state.isOnline,
                isTabSelected = selectedItem != -1,
                selectedItem = selectedItem,
                hazeState = hazeState,
                onItemClick = { index -> navController.navigateToTab(items[index].destination) }
            )
        }
    ) { innerPadding ->
        // No padding on the NavHost: content scrolls under the floating bar (and gets blurred);
        // lists add `bottomPadding` so their last item can still scroll above it.
        val bottomPadding = innerPadding.calculateBottomPadding()

        NavHost(
            navController = navController,
            startDestination = Destination.Home,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            composable<Destination.Home> {
                HomeRoute(bottomPadding = bottomPadding, navigateToDetails = navigateToDetails)
            }
            composable<Destination.Search> {
                BackToHomeHandler(navController)
                SearchRoute(bottomPadding = bottomPadding, navigateToDetails = navigateToDetails)
            }
            composable<Destination.Bookmark> {
                BackToHomeHandler(navController)
                BookmarkRoute(bottomPadding = bottomPadding, navigateToDetails = navigateToDetails)
            }
            composable<Destination.Details>(typeMap = Destination.Details.typeMap) {
                DetailRoute(navigateUp = { navController.navigateUp() })
            }
        }
    }
}

/** Offline pill + the floating tab bar (hidden with an animation on non-tab screens). */
@Composable
private fun FloatingBottomBar(
    isOnline: Boolean,
    isTabSelected: Boolean,
    selectedItem: Int,
    hazeState: HazeState,
    onItemClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavigatorDefaults.BarHorizontalMargin)
            .padding(bottom = bottomBarBottomMargin()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
    ) {
        AnimatedVisibility(visible = !isOnline, enter = fadeIn(), exit = fadeOut()) {
            OfflineBanner()
        }
        AnimatedVisibility(
            visible = isTabSelected,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            NewsBottomNavigation(
                items = NewsBottomNavigationItems,
                selectedItem = selectedItem,
                onItemClick = onItemClick,
                hazeState = hazeState
            )
        }
    }
}

/** Sit just above the gesture handle: overlap part of the navigation bar inset. */
@Composable
private fun bottomBarBottomMargin(): Dp {
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return (navBarInset - NavigatorDefaults.NavBarInsetOverlap).coerceAtLeast(NavigatorDefaults.MinBottomMargin)
}

/** Back from a non-home tab goes to Home (restoring its state) instead of leaving the app. */
@Composable
private fun BackToHomeHandler(navController: NavController) {
    BackHandler { navController.navigateToTab(Destination.Home) }
}

private fun NavController.navigateToTab(destination: Destination) {
    navigate(destination) {
        // Pop up to the start destination to avoid building up a large back stack,
        // saving each tab's state so it can be restored when re-selected
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
