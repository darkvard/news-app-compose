package com.example.news.presentation.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.common.ArticleList
import com.example.news.presentation.common.SearchBar
import com.example.news.util.Dimens
import kotlinx.coroutines.flow.flowOf

@Composable
fun SearchScreen(
    state: SearchState,
    event: (SearchEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = Dimens.ExtraSmallPadding6)
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        SearchBar(
            modifier = Modifier
                .padding(horizontal = Dimens.ExtraSmallPadding6)
                .fillMaxWidth(),
            text = state.searchQuery,
            readOnly = false,
            onValueChange = { event(SearchEvent.UpdateSearchQuery(it)) },
            onSearch = {
                event(SearchEvent.SearchNews)
            }
        )

        Spacer(modifier = Modifier.height(Dimens.MediumPadding1))

        state.articles?.let {
            ArticleList(
                modifier = Modifier.padding(horizontal = Dimens.ExtraSmallPadding6),
                articles = it.collectAsLazyPagingItems()
            ) {

            }
        }

    }
}

@Composable
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenPreview() {
    val dummyArticles = List(10) { index ->
        Article(
            author = "Author $index",
            content = "",
            description = "",
            publishedAt = "2 hours ago",
            source = Source(id = "", name = "BBC"),
            title = "Her train broke down. Her phone died. And then she met her Saver in a #$index",
            url = "",
            urlToImage = ""
        )
    }
    val articlesFlow = flowOf(PagingData.from(dummyArticles))

    SearchScreen(
        state = SearchState(searchQuery = "", articles = articlesFlow),
        event = {}
    )
}