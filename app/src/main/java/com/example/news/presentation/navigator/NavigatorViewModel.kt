package com.example.news.presentation.navigator

import androidx.lifecycle.viewModelScope
import com.example.news.domain.usecase.network.ObserveNetworkStatus
import com.example.news.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NavigatorViewModel @Inject constructor(
    observeNetworkStatus: ObserveNetworkStatus
) : MviViewModel<NavigatorState, Nothing, Nothing>(NavigatorState()) {

    init {
        observeNetworkStatus()
            .onEach { isOnline -> setState { copy(isOnline = isOnline) } }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: Nothing) = Unit
}
