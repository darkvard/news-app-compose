package com.example.news.presentation.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.news.usecase.news.NewsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class SearchViewModel @Inject constructor(
    val newsUseCases: NewsUseCases
) : ViewModel() {

    private var _state = mutableStateOf(SearchState())
    val state: State<SearchState> = _state

    private val searchQueryFlow = MutableStateFlow("")

    init {
        searchQueryFlow
            .debounce(350L)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach { query -> searchNews(query) }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.SearchNews -> {
                searchNews(_state.value.searchQuery)
            }

            is SearchEvent.UpdateSearchQuery -> {
                _state.value = _state.value.copy(searchQuery = event.searchQuery)
                searchQueryFlow.value = event.searchQuery
            }
        }
    }

    private fun searchNews(query: String) {
        val articles = newsUseCases.searchNews(
            searchQuery = query,
            sources = listOf("bbc-news", "abc-news", "google-news")
        ).cachedIn(viewModelScope)
        _state.value = _state.value.copy(articles = articles)
    }
} 