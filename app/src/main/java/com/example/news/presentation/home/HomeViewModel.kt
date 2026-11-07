package com.example.news.presentation.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.news.domain.model.Article
import com.example.news.domain.usecase.network.ObserveNetworkStatus
import com.example.news.domain.usecase.news.GetNews
import com.example.news.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getNews: GetNews,
    observeNetworkStatus: ObserveNetworkStatus
) : MviViewModel<HomeState, Nothing, Nothing>(HomeState()) {

    /**
     * Paged articles live outside [HomeState]: PagingData is a stream of pages managed by the
     * Paging library (with its own load states), not a value to copy into UI state.
     */
    val articles: Flow<PagingData<Article>> = getNews().cachedIn(viewModelScope)

    init {
        observeNetworkStatus()
            .onEach { isOnline -> setState { copy(isOnline = isOnline) } }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: Nothing) = Unit
}
