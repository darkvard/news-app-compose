package com.example.news.presentation.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.news.presentation.common.PagingArticleList
import com.example.news.presentation.common.RetryWhenBackOnline
import com.example.news.presentation.common.ScreenHeader
import com.example.news.presentation.common.SearchBar
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing
import kotlinx.coroutines.flow.flowOf

/** Stateful entry point: wires the ViewModel to the stateless [SearchScreen]. */
@Composable
fun SearchRoute(
    bottomPadding: Dp,
    navigateToDetails: (Article) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        query = viewModel.query,
        articles = viewModel.articles.collectAsLazyPagingItems(),
        bottomPadding = bottomPadding,
        onIntent = viewModel::onIntent,
        navigateToDetails = navigateToDetails
    )
}

@Composable
fun SearchScreen(
    state: SearchState,
    query: String,
    articles: LazyPagingItems<Article>,
    bottomPadding: Dp,
    onIntent: (SearchIntent) -> Unit,
    navigateToDetails: (Article) -> Unit
) {
    RetryWhenBackOnline(articles = articles, isOnline = state.isOnline)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ScreenHeader(title = stringResource(R.string.search))

        SearchBar(
            text = query,
            onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
            onSearch = { onIntent(SearchIntent.Submit) },
            modifier = Modifier.padding(horizontal = Spacing.ScreenHorizontal)
        )

        Spacer(modifier = Modifier.height(Spacing.Medium))

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
