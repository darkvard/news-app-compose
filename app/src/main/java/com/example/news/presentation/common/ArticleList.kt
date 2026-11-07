package com.example.news.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.ui.theme.Spacing

private object ArticleListDefaults {
    val ItemSpacing = 4.dp
    val ProgressSize = 24.dp
    val ProgressStrokeWidth = 2.dp

    /** Cards have their own inner padding, so the list padding is reduced to align with the headers. */
    val HorizontalPadding = Spacing.ScreenHorizontal - ArticleCardDefaults.ContentPadding

    const val ARTICLE_CONTENT_TYPE = "article"
    const val FOOTER_CONTENT_TYPE = "footer"
}

/**
 * Paged article list that renders every paging state:
 * - first load            -> shimmer
 * - first load failed     -> full-screen error + Retry
 * - loaded, nothing found -> [emptyContent]
 * - loading/failed more   -> footer (spinner / Retry) while keeping the list visible
 * - no more pages         -> footer with [endOfListMessage] (so the list never just stops silently)
 *
 * [bottomPadding] is the space taken by the floating bottom bar: the list scrolls *under* the bar
 * (so it can be blurred) but its last item can still scroll above it.
 *
 * [afterItem] is drawn right below each item (used to insert native ads in the feed without
 * the list or the paging data knowing anything about ads).
 */
@Composable
fun PagingArticleList(
    articles: LazyPagingItems<Article>,
    onClick: (Article) -> Unit,
    emptyContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    endOfListMessage: String? = null,
    bottomPadding: Dp = 0.dp,
    afterItem: @Composable (index: Int) -> Unit = {}
) {
    val loadState = articles.loadState
    val refresh = loadState.refresh

    if (articles.itemCount == 0) {
        val stateModifier = modifier.padding(bottom = bottomPadding)
        when {
            refresh is LoadState.Error -> ErrorState(
                error = refresh.error,
                onRetry = articles::retry,
                modifier = stateModifier
            )
            refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached ->
                Box(modifier = stateModifier) { emptyContent() }
            else -> LoadingState(modifier = stateModifier)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ArticleListDefaults.ItemSpacing),
        contentPadding = listContentPadding(bottomPadding)
    ) {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.url },
            contentType = articles.itemContentType { ArticleListDefaults.ARTICLE_CONTENT_TYPE }
        ) { index ->
            articles[index]?.let { article ->
                Column {
                    ArticleCard(article = article, onClick = { onClick(article) })
                    afterItem(index)
                }
            }
        }
        appendStateFooter(
            append = loadState.append,
            endOfListMessage = endOfListMessage,
            onRetry = articles::retry
        )
    }
}

/** Plain (non-paged) list, e.g. bookmarks coming straight from Room. Supports selection. */
@Composable
fun ArticleList(
    articles: List<Article>,
    onClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onLongClick: ((Article) -> Unit)? = null,
    isSelected: (Article) -> Boolean = { false }
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ArticleListDefaults.ItemSpacing),
        contentPadding = listContentPadding(bottomPadding)
    ) {
        items(
            items = articles,
            key = { it.url },
            contentType = { ArticleListDefaults.ARTICLE_CONTENT_TYPE }
        ) { article ->
            ArticleCard(
                article = article,
                isSelected = isSelected(article),
                onClick = { onClick(article) },
                onLongClick = onLongClick?.let { { it(article) } },
                modifier = Modifier.animateItem()
            )
        }
    }
}

private fun listContentPadding(bottomPadding: Dp) = PaddingValues(
    start = ArticleListDefaults.HorizontalPadding,
    end = ArticleListDefaults.HorizontalPadding,
    top = Spacing.ExtraSmall,
    bottom = bottomPadding + Spacing.Large
)

private fun LazyListScope.appendStateFooter(
    append: LoadState,
    endOfListMessage: String?,
    onRetry: () -> Unit
) {
    when {
        append is LoadState.Loading -> footer(key = "append_loading") {
            CircularProgressIndicator(
                modifier = Modifier.size(ArticleListDefaults.ProgressSize),
                strokeWidth = ArticleListDefaults.ProgressStrokeWidth
            )
        }

        append is LoadState.Error -> footer(key = "append_error") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.load_more_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NewsTextButton(text = stringResource(R.string.retry), onClick = onRetry)
            }
        }

        append.endOfPaginationReached && endOfListMessage != null -> footer(key = "end_of_list") {
            Text(
                text = endOfListMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun LazyListScope.footer(key: String, content: @Composable () -> Unit) {
    item(key = key, contentType = ArticleListDefaults.FOOTER_CONTENT_TYPE) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Large),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
