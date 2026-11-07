package com.example.news.presentation.home

import com.example.news.presentation.base.UiState

/**
 * Home has no intents/effects of its own: refresh & retry are paging operations on
 * `LazyPagingItems`, and the article pages are a separate Flow (see HomeViewModel.articles).
 */
data class HomeState(
    val isOnline: Boolean = true
) : UiState
