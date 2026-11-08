package com.example.news.presentation.bookmark.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.news.R
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing

private object DeleteDialogDefaults {
    val IconBadgeSize = 56.dp
    val IconSize = 28.dp
    val ButtonHeight = 48.dp
    val ShadowElevation = 8.dp
}

/**
 * Custom-styled confirm dialog (BasicAlertDialog gives us the dialog window + scrim,
 * the content is fully ours): icon badge, centered texts, two equal-width buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBookmarksDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Default dialog width is capped by the platform (narrow, almost square on phones):
    // opt out and go full width with the usual screen margin instead.
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(horizontal = Spacing.ScreenHorizontal),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DeleteBookmarksDialogContent(
            count = count,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DeleteBookmarksDialogContent(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.surfaceContainerLowest,
        tonalElevation = 0.dp,
        shadowElevation = DeleteDialogDefaults.ShadowElevation
    ) {
        Column(
            modifier = Modifier.padding(Spacing.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(DeleteDialogDefaults.IconBadgeSize)
                    .clip(CircleShape)
                    .background(colors.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(DeleteDialogDefaults.IconSize)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.Large))
            Text(
                text = pluralStringResource(R.plurals.delete_bookmarks_title, count, count),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.Small))
            Text(
                text = pluralStringResource(R.plurals.delete_bookmarks_message, count),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .weight(1f)
                        .height(DeleteDialogDefaults.ButtonHeight)
                ) {
                    Text(stringResource(R.string.cancel), style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onConfirm,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.onError
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(DeleteDialogDefaults.ButtonHeight)
                ) {
                    Text(stringResource(R.string.delete), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
@Preview
private fun DeleteBookmarksDialogPreview() {
    NewsTheme { DeleteBookmarksDialogContent(count = 3, onConfirm = {}, onDismiss = {}) }
}
