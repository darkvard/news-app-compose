package com.example.news.presentation.home.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.util.Dimens

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier,
    article: Article,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.clickable { onClick?.invoke() }
    ) {
        AsyncImage(
            modifier = Modifier
                .size(Dimens.ArticleCardSize)
                .clip(MaterialTheme.shapes.medium),
            model = ImageRequest.Builder(context).data(article.urlToImage).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .padding(horizontal = Dimens.ExtraSmallPadding3)
                .height(Dimens.ArticleCardSize)
        ) {
            Text(
                text = article.title ?: stringResource(R.string.no_title),
                style = MaterialTheme.typography.bodyMedium.copy(),
                color = colorResource(id = R.color.text_title),
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.source?.name ?: stringResource(R.string.unknown_source),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorResource(id = R.color.body)
                )
                Spacer(modifier = Modifier.width(Dimens.ExtraSmallPadding2))
                Icon(
                    painter = painterResource(id = R.drawable.ic_time),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.SmallIconSize),
                    tint = colorResource(id = R.color.body)
                )
                Spacer(modifier = Modifier.width(Dimens.ExtraSmallPadding))
                Text(
                    text = article.publishedAt ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(id = R.color.body)
                )
            }
        }
    }
}

@Composable
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ArticleCardPreview() {
    ArticleCard(
        article = Article(
            author = "",
            content = "",
            description = "",
            publishedAt = "2 hours",
            source = Source(id = "", name = "BBC"),
            title = "Her train broke down. Her phone died. And then she met her Saver in a",
            url = "",
            urlToImage = "https://ichef.bbci.co.uk/live-experience/cps/624/cpsprodpb/11787/production/_124395517_bbcbreakingnewsgraphic.jpg"
        ),
    )
}