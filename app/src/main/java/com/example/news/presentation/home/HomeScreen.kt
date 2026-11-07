package com.example.news.presentation.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.common.EmptyState
import com.example.news.presentation.common.PagingArticleList
import com.example.news.presentation.common.RetryWhenBackOnline
import com.example.news.presentation.common.ScreenHeader
import com.example.news.ui.theme.NewsTheme
import kotlinx.coroutines.flow.flowOf

/** Stateful entry point: wires the ViewModel to the stateless [HomeScreen]. */
@Composable
fun HomeRoute(
    bottomPadding: Dp,
    navigateToDetails: (Article) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        articles = viewModel.articles.collectAsLazyPagingItems(),
        bottomPadding = bottomPadding,
        navigateToDetails = navigateToDetails
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    articles: LazyPagingItems<Article>,
    bottomPadding: Dp,
    navigateToDetails: (Article) -> Unit
) {
    RetryWhenBackOnline(articles = articles, isOnline = state.isOnline)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ScreenHeader(title = stringResource(R.string.tab_news))

            PullToRefreshBox(
                isRefreshing = articles.loadState.refresh is LoadState.Loading && articles.itemCount > 0,
                onRefresh = articles::refresh,
                modifier = Modifier.weight(1f)
            ) {
                PagingArticleList(
                    articles = articles,
                    onClick = navigateToDetails,
                    bottomPadding = bottomPadding,
                    endOfListMessage = stringResource(R.string.home_end_of_list),
                    emptyContent = {
                        EmptyState(
                            icon = R.drawable.ic_search_document,
                            title = stringResource(R.string.home_empty_title),
                            message = stringResource(R.string.home_empty_message)
                        )
                    }
                )
            }
        }

        // Drawn last so it sits on top of the list, just above the floating bottom bar
        CachedDataRefreshErrorSnackbar(articles = articles, bottomPadding = bottomPadding)
    }
}

/**
 * Refresh failed but the cached feed is still on screen: keep it, and tell the user with a snackbar.
 * (When there is no cached data at all, [PagingArticleList] shows the full-screen error instead.)
 */
@Composable
private fun CachedDataRefreshErrorSnackbar(articles: LazyPagingItems<Article>, bottomPadding: Dp) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = stringResource(R.string.showing_cached_news)
    val retryLabel = stringResource(R.string.retry)
    val refreshFailedWithData = articles.loadState.refresh is LoadState.Error && articles.itemCount > 0

    LaunchedEffect(refreshFailedWithData) {
        if (refreshFailedWithData) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = retryLabel,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) articles.retry()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomPadding),
        contentAlignment = Alignment.BottomCenter
    ) {
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
@Preview(name = "light", showBackground = true)
@Preview("dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun HomeScreenPreview() {
    val dummyArticles = List(10) { index ->
        Article(
            url = "https://example.com/$index",
            publishedAt = "2023-06-16T22:24:33Z",
            source = Source(id = "", name = "BBC News"),
            title = "Her train broke down. Her phone died. And then she met her Saver in a #$index",
        )
    }
    NewsTheme {
        HomeScreen(
            state = HomeState(),
            articles = flowOf(PagingData.from(dummyArticles)).collectAsLazyPagingItems(),
            bottomPadding = 0.dp,
            navigateToDetails = {}
        )
    }
}
