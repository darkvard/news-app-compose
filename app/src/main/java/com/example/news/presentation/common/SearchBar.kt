package com.example.news.presentation.common

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.news.R
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing

private object SearchBarDefaults {
    val Height = 52.dp
    val FocusedBorderWidth = 1.5.dp
    val IconSize = 20.dp
    val StartPadding = 16.dp
    val EndPadding = 8.dp
    val ClearButtonTouchSize = 36.dp
    val ClearButtonCircleSize = 22.dp
    val ClearIconSize = 14.dp
    const val PlaceholderAlpha = 0.7f
    const val ClearCircleAlpha = 0.12f
}

/**
 * Built on BasicTextField (not Material TextField) so height, paddings and icon sizes
 * are fully under our control (see [SearchBarDefaults]).
 */
@Composable
fun SearchBar(
    text: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large

    BasicTextField(
        value = text,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(SearchBarDefaults.Height)
            .clip(shape)
            .background(colors.surfaceContainerHigh)
            .border(
                width = SearchBarDefaults.FocusedBorderWidth,
                color = if (isFocused) colors.primary else Color.Transparent,
                shape = shape
            ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        ),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(start = SearchBarDefaults.StartPadding, end = SearchBarDefaults.EndPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = if (isFocused) colors.primary else colors.onSurfaceVariant,
                    modifier = Modifier.size(SearchBarDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(Spacing.Medium))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant.copy(alpha = SearchBarDefaults.PlaceholderAlpha)
                        )
                    }
                    innerTextField()
                }
                AnimatedVisibility(
                    visible = text.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    ClearButton(onClick = { onValueChange("") })
                }
            }
        }
    )
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    // Big touch target, small icon on a subtle circle
    Box(
        modifier = Modifier
            .size(SearchBarDefaults.ClearButtonTouchSize)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(SearchBarDefaults.ClearButtonCircleSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SearchBarDefaults.ClearCircleAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.cd_clear_search),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SearchBarDefaults.ClearIconSize)
            )
        }
    }
}

@Composable
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun SearchBarPreview() {
    NewsTheme {
        Box(modifier = Modifier.padding(Spacing.Large)) {
            SearchBar(text = "bitcoin", onValueChange = {}, onSearch = {})
        }
    }
}
