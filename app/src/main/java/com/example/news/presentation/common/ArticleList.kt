package com.example.news.presentation.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.home.components.ArticleCard
import com.example.news.util.Dimens
import kotlinx.coroutines.flow.flowOf

@Composable
fun ArticleList(
    modifier: Modifier = Modifier,
    articles: LazyPagingItems<Article>,
    onClick: (Article) -> Unit
) {
    if (handlePagingResult(articles)) {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.MediumPadding1),
            contentPadding = PaddingValues(all = Dimens.ExtraSmallPadding)
        ) {
            items(
                count = articles.itemCount
            ) {
                articles[it]?.let {
                    ArticleCard(article = it, onClick = { onClick(it) })
                }
            }
        }
    }
}

@Composable
fun handlePagingResult(articles: LazyPagingItems<Article>): Boolean {
    val loadState = articles.loadState

    val error = when {
        loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
        loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
        loadState.append is LoadState.Error -> loadState.append as LoadState.Error
        else -> null
    }

    return when {
        loadState.refresh is LoadState.Loading -> {
            ShimmerEffect()
            false
        }

        error != null -> {
            EmptyScreen(error)
            false
        }

        else -> true
    }
}

@Composable
fun ShimmerEffect() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.MediumPadding1)) {
        repeat(10) {
            ArticleCardShimmerEffect(
                modifier = Modifier.padding(horizontal = Dimens.MediumPadding1)
            )
        }
    }
}


@Composable
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ArticleListPreview() {
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
    val articles = flowOf(PagingData.from(dummyArticles)).collectAsLazyPagingItems()

    ArticleList(articles = articles, onClick = {})
}
