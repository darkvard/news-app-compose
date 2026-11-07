package com.example.news.presentation.detail

import androidx.annotation.StringRes
import com.example.news.domain.model.Article
import com.example.news.presentation.base.UiEffect
import com.example.news.presentation.base.UiIntent
import com.example.news.presentation.base.UiState

data class DetailState(
    val article: Article,
    val isBookmarked: Boolean = false
) : UiState

sealed interface DetailIntent : UiIntent {
    data object ToggleBookmark : DetailIntent
    data object Share : DetailIntent
    data object OpenInBrowser : DetailIntent
}

/** Things that need an Android Context (toast, intents): the ViewModel asks, the UI performs. */
sealed interface DetailEffect : UiEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : DetailEffect
    data class ShareUrl(val url: String) : DetailEffect
    data class OpenUrl(val url: String) : DetailEffect
}
