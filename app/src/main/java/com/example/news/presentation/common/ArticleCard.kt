package com.example.news.presentation.common

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing

object ArticleCardDefaults {
    /** Public: lists/shimmer subtract it so card images line up with the screen padding. */
    val ContentPadding = 8.dp
    val ImageSize = 96.dp
    val ImageToTextSpacing = 12.dp
    val TitleToMetaSpacing = 8.dp
    const val TitleMaxLines = 3
    internal val BadgeSize = 26.dp
    internal val BadgeIconSize = 16.dp
    internal val BadgePadding = 6.dp
}

/**
 * One article row. [isSelected] / [onLongClick] are only used by the bookmark screen's
 * selection mode; Home and Search simply don't pass them.
 */
@Composable
fun ArticleCard(
    article: Article,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val publishedAt = remember(article.publishedAt) { formatPublishedAt(article.publishedAt) }
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "articleCardContainer"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(containerColor)
            .combinedClickable(
                enabled = onClick != null || onLongClick != null,
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick
            )
            .padding(ArticleCardDefaults.ContentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(context).data(article.urlToImage).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(ArticleCardDefaults.ImageSize)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
            // Fully qualified: inside a Row, the RowScope overload would otherwise be picked
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.padding(ArticleCardDefaults.BadgePadding)
            ) {
                SelectedBadge()
            }
        }

        Spacer(modifier = Modifier.width(ArticleCardDefaults.ImageToTextSpacing))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.title ?: stringResource(R.string.no_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = ArticleCardDefaults.TitleMaxLines,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(ArticleCardDefaults.TitleToMetaSpacing))
            ArticleMeta(sourceName = article.source?.name, publishedAt = publishedAt)
        }
    }
}

@Composable
private fun ArticleMeta(sourceName: String?, publishedAt: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = sourceName ?: stringResource(R.string.unknown_source),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (publishedAt.isNotEmpty()) {
            Text(
                text = "  •  $publishedAt",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SelectedBadge() {
    Box(
        modifier = Modifier
            .size(ArticleCardDefaults.BadgeSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(ArticleCardDefaults.BadgeIconSize)
        )
    }
}

@Composable
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun ArticleCardPreview() {
    NewsTheme {
        Column(modifier = Modifier.padding(Spacing.Medium)) {
            val article = Article(
                url = "https://example.com",
                publishedAt = "2023-06-16T22:24:33Z",
                source = Source(id = "", name = "BBC News"),
                title = "Her train broke down. Her phone died. And then she met her Saver in a",
            )
            ArticleCard(article = article, onClick = {})
            ArticleCard(article = article, isSelected = true, onClick = {})
        }
    }
}
