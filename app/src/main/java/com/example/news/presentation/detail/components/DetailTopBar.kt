package com.example.news.presentation.detail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.news.R
import com.example.news.ui.theme.NewsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(
    isBookmarked: Boolean,
    onBrowsingClick: () -> Unit,
    onShareClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
        actions = {
            IconButton(onClick = onBookmarkClick) {
                AnimatedContent(
                    targetState = isBookmarked,
                    transitionSpec = { scaleIn(spring()) togetherWith scaleOut() },
                    label = "bookmarkIcon"
                ) { bookmarked ->
                    Icon(
                        imageVector = if (bookmarked) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(
                            if (bookmarked) R.string.cd_remove_bookmark else R.string.cd_add_bookmark
                        ),
                        tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onShareClick) {
                Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.cd_share))
            }
            IconButton(onClick = onBrowsingClick) {
                Icon(Icons.Outlined.Public, contentDescription = stringResource(R.string.cd_open_in_browser))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
@Preview(showBackground = true)
private fun DetailTopBarPreview() {
    NewsTheme {
        DetailTopBar(isBookmarked = true, onBrowsingClick = {}, onShareClick = {}, onBookmarkClick = {}, onBackClick = {})
    }
}
