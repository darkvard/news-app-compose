package com.example.news.presentation.search

import com.example.news.presentation.base.UiIntent
import com.example.news.presentation.base.UiState

data class SearchState(
    /** The query results are shown for ("" = nothing searched yet). */
    val submittedQuery: String = "",
    val isOnline: Boolean = true
) : UiState

sealed interface SearchIntent : UiIntent {
    data class QueryChanged(val query: String) : SearchIntent

    /** Keyboard "search" action: run the search now instead of waiting for the debounce. */
    data object Submit : SearchIntent
}
