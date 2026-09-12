package com.example.news.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.news.presentation.common.NewsButton
import com.example.news.presentation.common.NewsTextButton
import com.example.news.presentation.onboarding.components.OnBoardingPage
import com.example.news.presentation.onboarding.components.PageIndicator
import com.example.news.util.Dimens
import kotlinx.coroutines.launch
import kotlin.collections.listOf

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewmodel: OnboardingViewModel = hiltViewModel()
) {
    OnboardingContent(modifier, viewmodel)
}

@Composable
private fun OnboardingContent(
    modifier: Modifier = Modifier,
    viewmodel: OnboardingViewModel = hiltViewModel()
) {
    Column(modifier = modifier.fillMaxSize()) {
        val pages = Page.getPages()

        val pageState = rememberPagerState(initialPage = 0) {
            pages.size
        }
        val buttonState = remember {
            derivedStateOf {
                when (pageState.currentPage) {
                    0 -> listOf("", "Next")
                    1 -> listOf("Back", "Next")
                    2 -> listOf("Back", "Next")
                    3 -> listOf("Back", "Next")
                    4 -> listOf("Back", "Get Started")
                    else -> listOf("", "")
                }
            }
        }

        HorizontalPager(state = pageState) {
            OnBoardingPage(page = pages[it])
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.MediumPadding2, vertical = Dimens.MediumPadding0)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageIndicator(
                pageSize = pages.size,
                selectedPage = pageState.currentPage,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                val scope = rememberCoroutineScope()
                if (buttonState.value[0].isNotEmpty()) {
                    NewsTextButton(
                        text = buttonState.value[0],
                        onClick = {
                            scope.launch {
                                pageState.animateScrollToPage(
                                    page = pageState.currentPage - 1
                                )
                            }
                        }
                    )
                }
                NewsButton(
                    text = buttonState.value[1]
                ) {
                    scope.launch {
                        if (pageState.currentPage == pages.size - 1) {
                            viewmodel.onEvent(OnboardingEvent.SaveAppEntry)
                        } else {
                            pageState.animateScrollToPage(
                                page = pageState.currentPage + 1
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun OnboardingScreenPreview() {
    OnboardingScreen()
}