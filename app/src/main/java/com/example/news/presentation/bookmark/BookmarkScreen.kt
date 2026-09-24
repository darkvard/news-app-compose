package com.example.news.presentation.bookmark


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.common.ArticleList
import com.example.news.util.Dimens
import com.example.news.util.Dimens.MediumPadding1
import kotlinx.coroutines.flow.flowOf

@Composable
fun BookmarkScreen(
    state: BookmarkState,
    navigateToDetails: (Article) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Dimens.MediumPadding1)
            .statusBarsPadding()
    ) {

        Text(
            text = "Bookmark",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colorResource(id = R.color.text_title),
            modifier = Modifier.padding(horizontal = Dimens.ExtraSmallPadding6)
        )

        Spacer(modifier = Modifier.height(MediumPadding1))

        ArticleList(
            articles = flowOf(PagingData.from(state.articles)).collectAsLazyPagingItems(),
            onClick = navigateToDetails,
            modifier = Modifier.padding(horizontal = Dimens.ExtraSmallPadding6)
        )
    }
}

@Composable
@Preview
fun BookmarkScreenPreview() {
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
    val data = BookmarkState(dummyArticles)
    BookmarkScreen(data) {

    }
}