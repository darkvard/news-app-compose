package com.example.news.presentation.bookmark

import com.example.news.domain.model.Article
import com.example.news.presentation.base.UiEffect
import com.example.news.presentation.base.UiIntent
import com.example.news.presentation.base.UiState

data class BookmarkState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    /** Urls of the selected articles. Non-empty = selection mode. */
    val selectedUrls: Set<String> = emptySet(),
    val showDeleteDialog: Boolean = false
) : UiState {
    val isSelectionMode: Boolean get() = selectedUrls.isNotEmpty()
    val selectedCount: Int get() = selectedUrls.size
    val allSelected: Boolean get() = articles.isNotEmpty() && selectedUrls.size == articles.size
}

sealed interface BookmarkIntent : UiIntent {
    /** Long press on an article: start selection mode with that article selected. */
    data class EnterSelection(val url: String) : BookmarkIntent

    /** Tap while in selection mode. Deselecting the last one exits selection mode. */
    data class ToggleSelection(val url: String) : BookmarkIntent

    data object ToggleSelectAll : BookmarkIntent
    data object ClearSelection : BookmarkIntent

    /** Trash icon: ask for confirmation first. */
    data object DeleteClick : BookmarkIntent
    data object ConfirmDelete : BookmarkIntent
    data object DismissDelete : BookmarkIntent
}

sealed interface BookmarkEffect : UiEffect {
    data class ShowDeleted(val count: Int) : BookmarkEffect
    data object ShowDeleteFailed : BookmarkEffect
}
