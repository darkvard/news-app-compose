package com.example.news.presentation.mainActivity

import androidx.lifecycle.viewModelScope
import com.example.news.domain.usecase.app_entry.ReadAppEntry
import com.example.news.presentation.base.MviViewModel
import com.example.news.presentation.navgraph.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    readAppEntry: ReadAppEntry,
) : MviViewModel<MainState, Nothing, Nothing>(MainState()) {

    init {
        // Keeps observing: finishing onboarding flips the start destination to the news graph
        readAppEntry()
            .onEach { onboardingDone ->
                setState {
                    copy(
                        isLoading = false,
                        startDestination = if (onboardingDone) Destination.NewsGraph else Destination.AppStartGraph
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: Nothing) = Unit
}
