package com.example.news.presentation.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private object PageIndicatorDefaults {
    val DotSize = 14.dp
    val DotSpacing = 4.dp
}

@Composable
fun PageIndicator(
    modifier: Modifier = Modifier,
    pageSize: Int,
    selectedPage: Int,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        repeat(pageSize) {
            Box(
                modifier = Modifier
                    .padding(end = PageIndicatorDefaults.DotSpacing)
                    .size(PageIndicatorDefaults.DotSize)
                    .clip(CircleShape)
                    .background(
                        if (selectedPage == it) {
                            selectedColor
                        } else {
                            unselectedColor
                        }
                    )
            )
        }
    }
}


@Composable
@Preview
private fun PageIndicatorPreview() {
    PageIndicator(
        pageSize = 5,
        selectedPage = 2,
    )
}
