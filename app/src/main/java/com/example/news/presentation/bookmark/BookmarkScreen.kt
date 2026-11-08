package com.example.news.presentation.bookmark

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.bookmark.components.DeleteBookmarksDialog
import com.example.news.presentation.bookmark.components.SelectionTopBar
import com.example.news.presentation.common.ArticleList
import com.example.news.presentation.common.EmptyState
import com.example.news.presentation.common.ObserveAsEvents
import com.example.news.presentation.common.ScreenHeader
import com.example.news.ui.adaptive.LANDSCAPE_PHONE_PREVIEW
import com.example.news.ui.theme.NewsTheme
import kotlinx.coroutines.launch

/** Stateful entry point: wires the ViewModel (state + effects) to the stateless [BookmarkScreen]. */
@Composable
fun BookmarkRoute(
    bottomPadding: Dp,
    navigateToDetails: (Article) -> Unit,
    viewModel: BookmarkViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    ObserveAsEvents(viewModel.effects) { effect ->
        val message = when (effect) {
            is BookmarkEffect.ShowDeleted ->
                resources.getQuantityString(R.plurals.bookmarks_deleted, effect.count, effect.count)
            BookmarkEffect.ShowDeleteFailed -> resources.getString(R.string.bookmark_delete_failed)
        }
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    BookmarkScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        bottomPadding = bottomPadding,
        onIntent = viewModel::onIntent,
        navigateToDetails = navigateToDetails
    )
}

@Composable
fun BookmarkScreen(
    state: BookmarkState,
    snackbarHostState: SnackbarHostState,
    bottomPadding: Dp,
    onIntent: (BookmarkIntent) -> Unit,
    navigateToDetails: (Article) -> Unit
) {
    // Back leaves selection mode first (declared after the tab's back handler, so it wins)
    BackHandler(enabled = state.isSelectionMode) { onIntent(BookmarkIntent.ClearSelection) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = state.isSelectionMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "bookmarkHeader"
            ) { isSelectionMode ->
                if (isSelectionMode) {
                    SelectionTopBar(
                        selectedCount = state.selectedCount,
                        allSelected = state.allSelected,
                        onClose = { onIntent(BookmarkIntent.ClearSelection) },
                        onToggleSelectAll = { onIntent(BookmarkIntent.ToggleSelectAll) },
                        onDelete = { onIntent(BookmarkIntent.DeleteClick) }
                    )
                } else {
                    ScreenHeader(title = stringResource(R.string.bookmark))
                }
            }

            when {
                state.isLoading -> Unit // Room answers in a few ms; avoid flashing a loader
                state.articles.isEmpty() -> EmptyState(
                    icon = R.drawable.ic_bookmark,
                    title = stringResource(R.string.bookmark_empty_title),
                    message = stringResource(R.string.bookmark_empty_message),
                    modifier = Modifier.padding(bottom = bottomPadding)
                )

                else -> ArticleList(
                    articles = state.articles,
                    bottomPadding = bottomPadding,
                    isSelected = { it.url in state.selectedUrls },
                    // Navigation is a UI decision: in selection mode a tap selects instead
                    onClick = { article ->
                        if (state.isSelectionMode) {
                            onIntent(BookmarkIntent.ToggleSelection(article.url))
                        } else {
                            navigateToDetails(article)
                        }
                    },
                    onLongClick = { article ->
                        onIntent(
                            if (state.isSelectionMode) {
                                BookmarkIntent.ToggleSelection(article.url)
                            } else {
                                BookmarkIntent.EnterSelection(article.url)
                            }
                        )
                    }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding)
        )
    }

    if (state.showDeleteDialog) {
        DeleteBookmarksDialog(
            count = state.selectedCount,
            onConfirm = { onIntent(BookmarkIntent.ConfirmDelete) },
            onDismiss = { onIntent(BookmarkIntent.DismissDelete) }
        )
    }
}

private val previewArticles = List(5) { index ->
    Article(
        url = "https://example.com/$index",
        publishedAt = "2023-06-16T22:24:33Z",
        source = Source(id = "", name = "BBC News"),
        title = "Her train broke down. Her phone died. And then she met her Saver in a #$index",
    )
}

@Composable
private fun BookmarkScreenPreview(state: BookmarkState) {
    NewsTheme {
        BookmarkScreen(
            state = state,
            snackbarHostState = remember { SnackbarHostState() },
            bottomPadding = 0.dp,
            onIntent = {},
            navigateToDetails = {}
        )
    }
}

@Composable
@Preview(name = "light", showBackground = true)
@Preview(name = "landscape", device = LANDSCAPE_PHONE_PREVIEW, showBackground = true)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun BookmarkListPreview() =
    BookmarkScreenPreview(BookmarkState(articles = previewArticles, isLoading = false))

@Composable
@Preview(name = "selection", showBackground = true)
private fun BookmarkSelectionPreview() = BookmarkScreenPreview(
    BookmarkState(
        articles = previewArticles,
        isLoading = false,
        selectedUrls = setOf(previewArticles[0].url, previewArticles[2].url)
    )
)

@Composable
@Preview(name = "empty", showBackground = true)
private fun BookmarkEmptyPreview() = BookmarkScreenPreview(BookmarkState(isLoading = false))
