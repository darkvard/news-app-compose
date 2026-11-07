package com.example.news.presentation.onboarding.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.news.R
import com.example.news.presentation.onboarding.Page
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing
import androidx.compose.ui.unit.dp

private object OnBoardingPageDefaults {
    const val ImageHeightFraction = 0.6f
    val TextHorizontalPadding = 30.dp
}

@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    page: Page
) {
    Column(modifier = modifier) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(OnBoardingPageDefaults.ImageHeightFraction),
            painter = painterResource(id = page.image),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
        Text(
            modifier = Modifier.padding(horizontal = OnBoardingPageDefaults.TextHorizontalPadding),
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            modifier = Modifier.padding(horizontal = OnBoardingPageDefaults.TextHorizontalPadding),
            text = page.description,
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
                page = Page(
                    title = "Lorem Ipsum is simply dummy",
                    description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                    image = R.drawable.img_ob_1
                )
            )
        }
    }
}