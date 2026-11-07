package com.example.news.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.news.domain.model.Article
import com.example.news.domain.usecase.network.ObserveNetworkStatus
import com.example.news.domain.usecase.news.SearchNews
import com.example.news.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchNews: SearchNews,
    observeNetworkStatus: ObserveNetworkStatus
) : MviViewModel<SearchState, SearchIntent, Nothing>(SearchState()) {

    /**
     * The text-field value is the one thing kept OUT of [SearchState]: a TextField must be updated
     * synchronously on every keystroke, and an async StateFlow round-trip can make the cursor jump
     * (Google's guidance for TextField state). Everything derived from it goes through the state.
     */
    var query by mutableStateOf("")
        private set

    private val typedQuery = MutableStateFlow("")

    /** Re-searches only when the submitted query changes (not on e.g. network changes). */
    val articles: Flow<PagingData<Article>> = state
        .map { it.submittedQuery }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(PagingData.empty()) else searchNews(query)
        }
        .cachedIn(viewModelScope)

    init {
        typedQuery
            .debounce(SEARCH_DEBOUNCE_MILLIS)
            .onEach(::submit)
            .launchIn(viewModelScope)

        observeNetworkStatus()
            .onEach { isOnline -> setState { copy(isOnline = isOnline) } }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> {
                query = intent.query
                typedQuery.value = intent.query
                if (intent.query.isBlank()) submit("") // clear results right away
            }

            SearchIntent.Submit -> submit(query)
        }
    }

    // Same query twice = same state = no new emission, so the debounce never repeats a search
    private fun submit(query: String) = setState { copy(submittedQuery = query.trim()) }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
    }
}
