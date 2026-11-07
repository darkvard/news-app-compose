package com.example.news.presentation.navigator

import com.example.news.presentation.base.UiState

data class NavigatorState(
    /** Drives the offline banner above the bottom bar. Starts true so it doesn't flash on launch. */
    val isOnline: Boolean = true
) : UiState
