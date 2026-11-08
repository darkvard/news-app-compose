package com.example.news.presentation.bookmark.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.news.R
import com.example.news.presentation.common.ScreenHeaderDefaults
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing

private object SelectionTopBarDefaults {
    val DeleteIconSize = 20.dp
}

/** Replaces the screen header while articles are selected. Same height, so nothing jumps. */
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    allSelected: Boolean,
    onClose: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ScreenHeaderDefaults.height())
            .padding(start = Spacing.ExtraSmall, end = Spacing.Large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.cd_close_selection),
                tint = colors.onSurfaceVariant
            )
        }
        Text(
            text = pluralStringResource(R.plurals.selected_count, selectedCount, selectedCount),
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onToggleSelectAll) {
            Text(
                text = stringResource(if (allSelected) R.string.deselect_all else R.string.select_all),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
        FilledTonalIconButton(
            onClick = onDelete,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colors.errorContainer,
                contentColor = colors.error
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = stringResource(R.string.cd_delete_selected),
                modifier = Modifier.size(SelectionTopBarDefaults.DeleteIconSize)
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun SelectionTopBarPreview() {
    NewsTheme {
        SelectionTopBar(selectedCount = 2, allSelected = false, onClose = {}, onToggleSelectAll = {}, onDelete = {})
    }
}
