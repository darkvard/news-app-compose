package com.example.news.presentation.onboarding

import com.example.news.presentation.base.UiIntent
import com.example.news.presentation.base.UiState

data class OnboardingState(
    val pages: List<Page> = Page.all
) : UiState

sealed interface OnboardingIntent : UiIntent {
    /** "Get Started" on the last page. MainViewModel observes the saved flag and switches graphs. */
    data object Finish : OnboardingIntent
}
