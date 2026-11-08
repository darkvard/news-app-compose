package com.example.news.presentation.navigator

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import com.example.news.ads.LocalAdsManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavHostController
import com.example.news.presentation.navigator.components.FloatingNavDefaults
import com.example.news.presentation.navigator.components.NewsNavigationRail
import com.example.news.ui.adaptive.rememberWindowLayout
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    /** Gap between the rail and the left edge (or the camera cutout) and the content. */
    val RailMargin = 12.dp

    /** The bar overlaps this much of the (mostly empty) gesture-handle inset... */
    val NavBarInsetOverlap = 4.dp

    /** ...but always keeps at least this gap to the bottom edge. */
    val MinBottomMargin = 8.dp
}

/**
 * Hosts the tab navigation and the destinations. Each destination is a `XxxRoute`.
 * The tabs sit in a floating bottom bar, or in a vertical rail on the left when the window is
 * short (phone in landscape) or wide (tablet): see [rememberWindowLayout].
 */
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
    val onTabClick: (Int) -> Unit = { index -> navController.navigateToTab(items[index].destination) }
    val ads = LocalAdsManager.current
    val activity = LocalActivity.current
    // Navigate first, then (maybe) show an interstitial on top: when it closes, the article is there
    val navigateToDetails: (Article) -> Unit = { article ->
        navController.navigate(Destination.Details(article))
        activity?.let(ads::onArticleOpened)
    }

    // Content behind the floating bar/rail is the haze "source"; the bar blurs it
    val hazeState = rememberHazeState()

    if (rememberWindowLayout().useNavigationRail) {
        val layoutDirection = LocalLayoutDirection.current
        val safeArea = WindowInsets.safeDrawing.asPaddingValues()
        val railStart = safeArea.calculateStartPadding(layoutDirection) + NavigatorDefaults.RailMargin
        // Space taken at the bottom by the offline banner (0 when online), so it never covers
        // content such as the "Retry" button. (The bottom-bar layout gets this from Scaffold.)
        val density = LocalDensity.current
        val bannerMargin = bottomBarBottomMargin()
        var offlineBannerSpace by remember { mutableStateOf(0.dp) }

        Box(modifier = Modifier.fillMaxSize()) {
            NewsNavHost(
                navController = navController,
                hazeState = hazeState,
                navigateToDetails = navigateToDetails,
                tabContentPadding = PaddingValues(
                    start = railStart + FloatingNavDefaults.Thickness,
                    end = safeArea.calculateEndPadding(layoutDirection),
                    bottom = maxOf(safeArea.calculateBottomPadding(), offlineBannerSpace)
                )
            )
            AnimatedVisibility(
                visible = selectedItem != -1,
                enter = slideInHorizontally { -it } + fadeIn(),
                exit = slideOutHorizontally { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = railStart)
            ) {
                NewsNavigationRail(
                    items = items,
                    selectedItem = selectedItem,
                    onItemClick = onTabClick,
                    hazeState = hazeState
                )
            }
            AnimatedVisibility(
                visible = !state.isOnline,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bannerMargin)
                    .onSizeChanged { size ->
                        offlineBannerSpace = if (size.height == 0) {
                            0.dp
                        } else {
                            with(density) { size.height.toDp() } + bannerMargin + Spacing.Small
                        }
                    }
            ) {
                OfflineBanner()
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                FloatingBottomBar(
                    isOnline = state.isOnline,
                    isTabSelected = selectedItem != -1,
                    selectedItem = selectedItem,
                    hazeState = hazeState,
                    onItemClick = onTabClick
                )
            }
        ) { innerPadding ->
            NewsNavHost(
                navController = navController,
                hazeState = hazeState,
                navigateToDetails = navigateToDetails,
                tabContentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
            )
        }
    }
}

/**
 * The destinations. No padding on the NavHost itself: content scrolls under the floating bar
 * (and gets blurred). Tabs get [tabContentPadding] instead: start/end keep them clear of the rail
 * and the screen cutouts, bottom lets the last list item scroll above the bar.
 */
@Composable
private fun NewsNavHost(
    navController: NavHostController,
    hazeState: HazeState,
    navigateToDetails: (Article) -> Unit,
    tabContentPadding: PaddingValues
) {
    val layoutDirection = LocalLayoutDirection.current
    val bottomPadding = tabContentPadding.calculateBottomPadding()
    val tabModifier = Modifier.padding(
        start = tabContentPadding.calculateStartPadding(layoutDirection),
        end = tabContentPadding.calculateEndPadding(layoutDirection)
    )

    NavHost(
        navController = navController,
        startDestination = Destination.Home,
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
    ) {
        composable<Destination.Home> {
            Box(tabModifier) {
                HomeRoute(bottomPadding = bottomPadding, navigateToDetails = navigateToDetails)
            }
        }
        composable<Destination.Search> {
            BackToHomeHandler(navController)
            Box(tabModifier) {
                SearchRoute(bottomPadding = bottomPadding, navigateToDetails = navigateToDetails)
            }
        }
        composable<Destination.Bookmark> {
            BackToHomeHandler(navController)
            Box(tabModifier) {
                BookmarkRoute(bottomPadding = bottomPadding, navigateToDetails = navigateToDetails)
            }
        }
        composable<Destination.Details>(typeMap = Destination.Details.typeMap) {
            DetailRoute(navigateUp = { navController.navigateUp() })
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
