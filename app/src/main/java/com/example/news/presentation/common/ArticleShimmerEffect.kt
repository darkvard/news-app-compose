package com.example.news.presentation.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.news.ui.theme.NewsTheme

private object ShimmerDefaults {
    const val DurationMillis = 900
    const val MinAlpha = 0.4f
    const val MaxAlpha = 1f
    val TitleLineHeight = 16.dp
    val MetaLineHeight = 12.dp
    val LineSpacing = 6.dp
    val TitleToMetaSpacing = 12.dp
}

fun Modifier.shimmerEffect() = composed {
    val alpha = rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = ShimmerDefaults.MinAlpha,
        targetValue = ShimmerDefaults.MaxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(ShimmerDefaults.DurationMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    ).value
    background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha))
}

/** Same layout as [ArticleCard] (uses its defaults) so nothing jumps when real data arrives. */
@Composable
fun ArticleCardShimmerEffect(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(ArticleCardDefaults.ContentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(ArticleCardDefaults.ImageSize)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.width(ArticleCardDefaults.ImageToTextSpacing))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerLine(widthFraction = 1f, height = ShimmerDefaults.TitleLineHeight)
            Spacer(modifier = Modifier.height(ShimmerDefaults.LineSpacing))
            ShimmerLine(widthFraction = 0.8f, height = ShimmerDefaults.TitleLineHeight)
            Spacer(modifier = Modifier.height(ShimmerDefaults.TitleToMetaSpacing))
            ShimmerLine(widthFraction = 0.4f, height = ShimmerDefaults.MetaLineHeight)
        }
    }
}

@Composable
private fun ShimmerLine(widthFraction: Float, height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(MaterialTheme.shapes.extraSmall)
            .shimmerEffect()
    )
}

@Composable
@Preview(showBackground = true)
private fun ArticleCardShimmerEffectPreview() {
    NewsTheme { ArticleCardShimmerEffect() }
}
