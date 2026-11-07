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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.news.R
import com.example.news.presentation.common.NewsButton
import com.example.news.presentation.common.NewsTextButton
import com.example.news.presentation.onboarding.components.OnBoardingPage
import com.example.news.presentation.onboarding.components.PageIndicator
import com.example.news.ui.theme.NewsTheme
import com.example.news.ui.theme.Spacing
import kotlinx.coroutines.launch

private object OnboardingDefaults {
    val ControlsHorizontalPadding = 30.dp
}

/** Stateful entry point: wires the ViewModel to the stateless [OnboardingScreen]. */
@Composable
fun OnboardingRoute(viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnboardingScreen(state = state, onIntent = viewModel::onIntent)
}

/** Which page is shown is pure UI state, so it lives in [PagerState], not in the ViewModel. */
@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0) { state.pages.size }

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState) { index ->
            OnBoardingPage(page = state.pages[index])
        }
        Spacer(modifier = Modifier.weight(1f))
        OnboardingControls(
            pagerState = pagerState,
            onFinish = { onIntent(OnboardingIntent.Finish) }
        )
    }
}

@Composable
private fun OnboardingControls(pagerState: PagerState, onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()
    val isFirstPage = pagerState.currentPage == 0
    val isLastPage = pagerState.currentPage == pagerState.pageCount - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OnboardingDefaults.ControlsHorizontalPadding, vertical = Spacing.Large)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PageIndicator(pageSize = pagerState.pageCount, selectedPage = pagerState.currentPage)

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isFirstPage) {
                NewsTextButton(text = stringResource(R.string.onboarding_back)) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }
            }
            NewsButton(
                text = stringResource(if (isLastPage) R.string.onboarding_get_started else R.string.onboarding_next)
            ) {
                if (isLastPage) {
                    onFinish()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun OnboardingScreenPreview() {
    NewsTheme { OnboardingScreen(state = OnboardingState(), onIntent = {}) }
}
