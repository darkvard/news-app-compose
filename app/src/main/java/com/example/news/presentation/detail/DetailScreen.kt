package com.example.news.presentation.detail

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.news.R
import com.example.news.ads.LocalAdsManager
import com.example.news.ads.banner.BannerAd
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.common.NewsButton
import com.example.news.presentation.common.ObserveAsEvents
import com.example.news.presentation.common.formatPublishedAt
import com.example.news.presentation.detail.components.DetailTopBar
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing

private object DetailDefaults {
    const val ImageAspectRatio = 16f / 9f
    val ButtonHeight = 52.dp
}

/** Stateful entry point: wires the ViewModel (state + effects) to the stateless [DetailScreen]. */
@Composable
fun DetailRoute(
    navigateUp: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val ads = LocalAdsManager.current
    val canShowAds by ads.canShowAds.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is DetailEffect.ShowMessage ->
                Toast.makeText(context, effect.messageRes, Toast.LENGTH_SHORT).show()
            is DetailEffect.ShareUrl -> {
                ads.skipNextAppOpen() // leaving on purpose: no App Open when coming back
                context.shareUrl(effect.url)
            }
            is DetailEffect.OpenUrl -> {
                ads.skipNextAppOpen()
                context.openInBrowser(effect.url)
            }
        }
    }

    DetailScreen(
        state = state,
        onIntent = viewModel::onIntent,
        navigateUp = navigateUp,
        bottomBanner = { if (canShowAds) BannerAd() }
    )
}

@Composable
fun DetailScreen(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit,
    navigateUp: () -> Unit,
    bottomBanner: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        DetailTopBar(
            isBookmarked = state.isBookmarked,
            onBrowsingClick = { onIntent(DetailIntent.OpenInBrowser) },
            onShareClick = { onIntent(DetailIntent.Share) },
            onBookmarkClick = { onIntent(DetailIntent.ToggleBookmark) },
            onBackClick = navigateUp
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = Spacing.ScreenHorizontal,
                end = Spacing.ScreenHorizontal,
                top = Spacing.Small,
                bottom = Spacing.ExtraLarge
            )
        ) {
            item {
                ArticleDetailContent(
                    article = state.article,
                    onReadFullArticle = { onIntent(DetailIntent.OpenInBrowser) }
                )
            }
        }

        // Anchored banner: below the content, never overlapping it
        bottomBanner()
    }
}

@Composable
private fun ArticleDetailContent(article: Article, onReadFullArticle: () -> Unit) {
    val context = LocalContext.current
    val publishedAt = remember(article.publishedAt) { formatPublishedAt(article.publishedAt) }
    val body = remember(article) { article.readableBody() }

    Column {
        Text(
            text = article.title ?: stringResource(R.string.no_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        ArticleMetaLine(
            sourceName = article.source?.name ?: stringResource(R.string.unknown_source),
            author = article.author?.takeIf { it.isNotBlank() },
            publishedAt = publishedAt
        )
        if (!article.urlToImage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(Spacing.Large))
            AsyncImage(
                model = ImageRequest.Builder(context).data(article.urlToImage).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(DetailDefaults.ImageAspectRatio)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }
        if (body.isNotBlank()) {
            Spacer(modifier = Modifier.height(Spacing.ScreenHorizontal))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
        NewsButton(
            text = stringResource(R.string.read_full_article),
            onClick = onReadFullArticle,
            modifier = Modifier
                .fillMaxWidth()
                .height(DetailDefaults.ButtonHeight)
        )
    }
}

/** "BBC News · Jane Doe · 2 hours ago" with the source highlighted. */
@Composable
private fun ArticleMetaLine(sourceName: String, author: String?, publishedAt: String) {
    val colors = MaterialTheme.colorScheme
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = colors.primary)) { append(sourceName) }
        listOfNotNull(author, publishedAt.takeIf { it.isNotBlank() }).forEach {
            append("  •  ")
            append(it)
        }
    }
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
}

/** NewsAPI truncates `content` and appends "… [+1131 chars]"; drop that marker, fall back to description. */
private fun Article.readableBody(): String {
    val text = content?.takeIf { it.isNotBlank() } ?: description.orEmpty()
    return text.replace(TRUNCATION_MARKER, "").trim()
}

private val TRUNCATION_MARKER = Regex("""\s*\[\+\d+ chars]$""")

private fun Context.openInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    if (intent.resolveActivity(packageManager) != null) startActivity(intent)
}

private fun Context.shareUrl(url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }
    startActivity(Intent.createChooser(intent, null))
}

@Composable
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun DetailScreenPreview() {
    NewsTheme {
        DetailScreen(
            state = DetailState(
                article = Article(
                    url = "https://example.com",
                    author = "Jane Doe",
                    title = "Coinbase says Apple blocked its last app release on NFTs in Wallet",
                    content = "We use cookies and data to Deliver and maintain Google services Track outages and protect against spam, fraud, and abuse… [+1131 chars]",
                    publishedAt = "2023-06-16T22:24:33Z",
                    source = Source(id = "", name = "BBC News"),
                    urlToImage = "https://media.wired.com/photos/6495d5e893ba5cd8bbdc95af/191:100/w_1280,c_limit/The-EU-Rules-Phone-Batteries-Must-Be-Replaceable-Gear-2BE6PRN.jpg"
                ),
                isBookmarked = true
            ),
            onIntent = {},
            navigateUp = {}
        )
    }
}
