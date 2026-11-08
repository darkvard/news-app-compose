package com.example.news.presentation.detail

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import com.example.news.ui.adaptive.rememberWindowLayout
import com.example.news.ui.adaptive.LANDSCAPE_PHONE_PREVIEW
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing
import kotlinx.coroutines.launch

private object DetailDefaults {
    const val ImageAspectRatio = 16f / 9f

    /** Share of the width taken by the image in the two-pane (landscape) layout. */
    const val ImagePaneWeight = 0.4f
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
    val isCompactHeight = rememberWindowLayout().isCompactHeight
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is DetailEffect.ShowMessage -> {
                val message = context.getString(effect.messageRes)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss() // rapid toggles: show only the latest
                    snackbarHostState.showSnackbar(message)
                }
            }
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
        snackbarHostState = snackbarHostState,
        bottomBanner = { if (canShowAds) BannerAd(isCompact = isCompactHeight) }
    )
}

@Composable
fun DetailScreen(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit,
    navigateUp: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomBanner: @Composable () -> Unit = {}
) {
    // Short window (phone in landscape): image on the left, text on the right
    val twoPane = rememberWindowLayout().isCompactHeight && !state.article.urlToImage.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
    ) {
        DetailTopBar(
            isBookmarked = state.isBookmarked,
            onBrowsingClick = { onIntent(DetailIntent.OpenInBrowser) },
            onShareClick = { onIntent(DetailIntent.Share) },
            onBookmarkClick = { onIntent(DetailIntent.ToggleBookmark) },
            onBackClick = navigateUp
        )

        // The snackbar lives inside the content area, so the banner below can never cover it
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val onReadFullArticle = { onIntent(DetailIntent.OpenInBrowser) }
            if (twoPane) {
                Row(modifier = Modifier.fillMaxSize()) {
                    ArticleImage(
                        url = state.article.urlToImage,
                        modifier = Modifier
                            .weight(DetailDefaults.ImagePaneWeight)
                            .fillMaxHeight()
                            .padding(start = Spacing.ScreenHorizontal, top = Spacing.Small, bottom = Spacing.Large)
                    )
                    ArticleScrollColumn(modifier = Modifier.weight(1f - DetailDefaults.ImagePaneWeight)) {
                        ArticleText(article = state.article, onReadFullArticle = onReadFullArticle)
                    }
                }
            } else {
                ArticleScrollColumn(modifier = Modifier.fillMaxSize()) {
                    ArticleText(
                        article = state.article,
                        onReadFullArticle = onReadFullArticle,
                        image = {
                            ArticleImage(
                                url = state.article.urlToImage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(DetailDefaults.ImageAspectRatio)
                            )
                        }
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Anchored banner: below the content, never overlapping it
        bottomBanner()
    }
}

@Composable
private fun ArticleScrollColumn(modifier: Modifier, content: @Composable () -> Unit) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = Spacing.ScreenHorizontal,
            end = Spacing.ScreenHorizontal,
            top = Spacing.Small,
            bottom = Spacing.ExtraLarge
        )
    ) {
        item { content() }
    }
}

/** Nothing is drawn when the article has no image. */
@Composable
private fun ArticleImage(url: String?, modifier: Modifier = Modifier) {
    if (url.isNullOrBlank()) return
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    )
}

/** Title, meta line, [image] (single-pane only), body and the "Read full article" button. */
@Composable
private fun ArticleText(
    article: Article,
    onReadFullArticle: () -> Unit,
    image: (@Composable () -> Unit)? = null
) {
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
        if (image != null && !article.urlToImage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(Spacing.Large))
            image()
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
@Preview(name = "landscape", device = LANDSCAPE_PHONE_PREVIEW, showBackground = true)
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
