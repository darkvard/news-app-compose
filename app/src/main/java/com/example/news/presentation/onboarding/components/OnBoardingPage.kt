package com.example.news.presentation.onboarding.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.news.presentation.onboarding.Page
import com.example.news.ui.adaptive.rememberWindowLayout
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing
import androidx.compose.ui.unit.dp

object OnBoardingPageDefaults {
    const val ImageHeightFraction = 0.6f

    /** Two-pane (short window): share of the width taken by the image. Public: the controls align to it. */
    const val ImageWidthFraction = 0.45f
    val TextHorizontalPadding = 30.dp
}

/**
 * Image on top and text below; in a short window (phone in landscape) image on the left and
 * text on the right, so the text is never pushed off screen.
 */
@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    page: Page
) {
    if (rememberWindowLayout().isCompactHeight) {
        Row(modifier = modifier.fillMaxSize()) {
            PageImage(
                page = page,
                modifier = Modifier
                    .fillMaxWidth(OnBoardingPageDefaults.ImageWidthFraction)
                    .fillMaxHeight()
            )
            PageText(
                page = page,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = Spacing.ExtraLarge)
            )
        }
    } else {
        Column(modifier = modifier) {
            PageImage(
                page = page,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(OnBoardingPageDefaults.ImageHeightFraction)
            )
            Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
            PageText(page = page)
        }
    }
}

@Composable
private fun PageImage(page: Page, modifier: Modifier) {
    Image(
        modifier = modifier,
        painter = painterResource(id = page.image),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun PageText(page: Page, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(horizontal = OnBoardingPageDefaults.TextHorizontalPadding),
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            modifier = Modifier.padding(horizontal = OnBoardingPageDefaults.TextHorizontalPadding),
            text = stringResource(page.description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@Preview(name = "Light", showBackground = true, showSystemUi = true)
@Preview(
    name = "Dark",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
private fun OnBoardingPagePreview() {
    NewsTheme {
        Surface {
            OnBoardingPage(
                page = Page.all.first()
            )
        }
    }
}