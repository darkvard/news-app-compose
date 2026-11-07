package com.example.news.presentation.mainActivity

import com.example.news.presentation.navgraph.Destination
import com.example.news.presentation.base.UiState

data class MainState(
    /** Keeps the splash screen visible until we know where to start. */
    val isLoading: Boolean = true,
    val startDestination: Destination = Destination.AppStartGraph
) : UiState
