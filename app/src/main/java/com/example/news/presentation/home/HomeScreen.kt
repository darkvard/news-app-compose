package com.example.news.presentation.home

import android.content.res.Configuration
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.common.ArticleList
import com.example.news.presentation.common.SearchBar
import com.example.news.presentation.navgraph.Route
import com.example.news.util.Dimens
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeScreen(
    articles: LazyPagingItems<Article>, navigate: (String) -> Unit
) {
    val title by remember {
        derivedStateOf {
            if (articles.itemCount > 10) {
                articles.itemSnapshotList.items
                    .slice(IntRange(start = 0, endInclusive = 9))
                    .joinToString(separator = " 🟥 ") { it.title ?: "" }
            } else {
                ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Dimens.MediumPadding1)
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colorResource(id = R.color.text_title),
            modifier = Modifier.padding(horizontal = Dimens.ExtraSmallPadding6)
        )

        Spacer(modifier = Modifier.height(Dimens.MediumPadding1))

        SearchBar(
            modifier = Modifier
                .padding(horizontal = Dimens.ExtraSmallPadding6)
                .fillMaxWidth(),
            text = "",
            readOnly = true,
            onValueChange = {},
            onSearch = {},
            onClick = {
                navigate(Route.SearchScreen.route)
            }
        )

        Spacer(modifier = Modifier.height(Dimens.ExtraSmallPadding3))


        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee()
                .padding(start = Dimens.ExtraSmallPadding6),
            fontSize = 12.sp,
            color = colorResource(id = R.color.text_title),
        )

        Spacer(modifier = Modifier.height(Dimens.ExtraSmallPadding3))

        ArticleList(
            modifier = Modifier.padding(horizontal = Dimens.ExtraSmallPadding6),
            articles = articles,
            onClick = {
                //TODO: Navigate to Details Screen
            }
        )
    }
}

@Composable
@Preview(name = "light", showBackground = true)
@Preview("dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun HomeScreenPreview() {
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
    HomeScreen(articles) {

    }
}