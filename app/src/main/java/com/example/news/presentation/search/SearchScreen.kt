package com.example.news.presentation.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.news.ads.nativead.FeedNativeAd
import com.example.news.ads.nativead.FeedNativeAdsViewModel
import com.example.news.ads.policy.NativeAdPlacement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.common.EmptyState
import com.example.news.presentation.common.ListInsert
import com.example.news.presentation.common.PagingArticleList
import com.example.news.presentation.common.RetryWhenBackOnline
import com.example.news.presentation.common.ScreenHeader
import com.example.news.presentation.common.ScreenHeaderDefaults
import com.example.news.ui.adaptive.rememberWindowLayout
import com.example.news.presentation.common.SearchBar
import com.example.news.ui.adaptive.LANDSCAPE_PHONE_PREVIEW
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing
import kotlinx.coroutines.flow.flowOf

/** Stateful entry point: wires the ViewModel to the stateless [SearchScreen]. */
@Composable
fun SearchRoute(
    bottomPadding: Dp,
    navigateToDetails: (Article) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    nativeAdsViewModel: FeedNativeAdsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nativeAds = remember(nativeAdsViewModel) {
        val placement = NativeAdPlacement()
        ListInsert(isAfter = placement::isAdAfter) { index ->
            FeedNativeAd(index = index, placement = placement, viewModel = nativeAdsViewModel)
        }
    }
    SearchScreen(
        state = state,
        query = viewModel.query,
        articles = viewModel.articles.collectAsLazyPagingItems(),
        bottomPadding = bottomPadding,
        onIntent = viewModel::onIntent,
        navigateToDetails = navigateToDetails,
        nativeAds = nativeAds
    )
}

@Composable
fun SearchScreen(
    state: SearchState,
    query: String,
    articles: LazyPagingItems<Article>,
    bottomPadding: Dp,
    onIntent: (SearchIntent) -> Unit,
    navigateToDetails: (Article) -> Unit,
    nativeAds: ListInsert? = null
) {
    RetryWhenBackOnline(articles = articles, isOnline = state.isOnline)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        val searchBar: @Composable (Modifier) -> Unit = { modifier ->
            SearchBar(
                text = query,
                onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
                onSearch = { onIntent(SearchIntent.Submit) },
                modifier = modifier
            )
        }

        if (rememberWindowLayout().isCompactHeight) {
            // Short window: title and search bar share one row to leave room for results
            Row(
                modifier = Modifier.padding(horizontal = Spacing.ScreenHorizontal, vertical = Spacing.Small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge)
            ) {
                Text(
                    text = stringResource(R.string.search),
                    style = ScreenHeaderDefaults.titleStyle(),
                    color = MaterialTheme.colorScheme.onSurface
                )
                searchBar(Modifier.weight(1f))
            }
        } else {
            ScreenHeader(title = stringResource(R.string.search))
            searchBar(Modifier.padding(horizontal = Spacing.ScreenHorizontal))
            Spacer(modifier = Modifier.height(Spacing.Medium))
        }

        if (state.submittedQuery.isBlank()) {
            EmptyState(
                icon = R.drawable.ic_search,
                title = stringResource(R.string.search_prompt_title),
                message = stringResource(R.string.search_prompt_message),
                modifier = Modifier.padding(bottom = bottomPadding)
            )
        } else {
            PagingArticleList(
                articles = articles,
                onClick = navigateToDetails,
                bottomPadding = bottomPadding,
                endOfListMessage = stringResource(R.string.search_end_of_list),
                insert = nativeAds,
                emptyContent = {
                    EmptyState(
                        icon = R.drawable.ic_search_document,
                        title = stringResource(R.string.search_no_results_title),
                        message = stringResource(R.string.search_no_results_message, state.submittedQuery)
                    )
                }
            )
        }
    }
}

@Composable
@Preview(name = "light", showBackground = true)
@Preview(name = "landscape", device = LANDSCAPE_PHONE_PREVIEW, showBackground = true)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun SearchScreenPreview() {
    val dummyArticles = List(10) { index ->
        Article(
            url = "https://example.com/$index",
            publishedAt = "2023-06-16T22:24:33Z",
            source = Source(id = "", name = "BBC News"),
            title = "Her train broke down. Her phone died. And then she met her Saver in a #$index",
        )
    }
    NewsTheme {
        SearchScreen(
            state = SearchState(submittedQuery = "train"),
            query = "train",
            articles = flowOf(PagingData.from(dummyArticles)).collectAsLazyPagingItems(),
            bottomPadding = 0.dp,
            onIntent = {},
            navigateToDetails = {}
        )
    }
}
