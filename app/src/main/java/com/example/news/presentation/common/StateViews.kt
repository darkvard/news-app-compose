package com.example.news.presentation.common

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.news.R
import com.example.news.domain.model.DataError
import com.example.news.ui.adaptive.rememberWindowLayout
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing

private object StateViewsDefaults {
    const val ShimmerItemCount = 6
    val ShimmerItemSpacing = 4.dp
    val IconSize = 96.dp

    /** Short windows (phone in landscape): icon + texts + button must fit above the offline banner. */
    val CompactIconSize = 64.dp
    const val IconAlpha = 0.45f
    val OfflineIconSize = 18.dp
}

/** Shimmer placeholders shown while the first page loads (same paddings as the article list). */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = Spacing.ScreenHorizontal - ArticleCardDefaults.ContentPadding,
                vertical = Spacing.ExtraSmall
            ),
        verticalArrangement = Arrangement.spacedBy(StateViewsDefaults.ShimmerItemSpacing)
    ) {
        repeat(StateViewsDefaults.ShimmerItemCount) { ArticleCardShimmerEffect() }
    }
}

/** Centered icon + title + message (+ optional action). Scrollable so it also works inside pull-to-refresh. */
@Composable
fun MessageState(
    @DrawableRes icon: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val isCompact = rememberWindowLayout().isCompactHeight
    val gap = if (isCompact) Spacing.Medium else Spacing.ExtraLarge
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.Huge, vertical = gap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = StateViewsDefaults.IconAlpha),
            modifier = Modifier.size(if (isCompact) StateViewsDefaults.CompactIconSize else StateViewsDefaults.IconSize)
        )
        Spacer(modifier = Modifier.height(gap))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(gap))
            action()
        }
    }
}

@Composable
fun EmptyState(
    @DrawableRes icon: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) = MessageState(icon = icon, title = title, message = message, modifier = modifier)

@Composable
fun ErrorState(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorUi = error.toErrorUi()
    MessageState(
        icon = errorUi.icon,
        title = stringResource(errorUi.title),
        message = stringResource(errorUi.message, *errorUi.messageArgs.toTypedArray()),
        modifier = modifier,
        action = { NewsButton(text = stringResource(R.string.retry), onClick = onRetry) }
    )
}

/** Small floating pill, shown above the bottom bar while the device is offline. */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = Spacing.Large, vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(StateViewsDefaults.OfflineIconSize)
        )
        Spacer(modifier = Modifier.width(Spacing.Small))
        Text(
            text = stringResource(R.string.offline_banner),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun ErrorStatePreview() {
    NewsTheme {
        ErrorState(error = DataError.NoConnection(), onRetry = {})
    }
}

@Composable
@Preview(showBackground = true)
private fun OfflineBannerPreview() {
    NewsTheme { OfflineBanner() }
}
