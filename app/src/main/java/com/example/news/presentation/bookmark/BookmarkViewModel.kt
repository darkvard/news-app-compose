package com.example.news.presentation.bookmark

import androidx.lifecycle.viewModelScope
import com.example.news.domain.usecase.news.DeleteBookmarks
import com.example.news.domain.usecase.news.ObserveBookmarks
import com.example.news.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    observeBookmarks: ObserveBookmarks,
    private val deleteBookmarks: DeleteBookmarks
) : MviViewModel<BookmarkState, BookmarkIntent, BookmarkEffect>(BookmarkState()) {

    init {
        // Room re-emits on every insert/delete, so the list is always up to date
        observeBookmarks()
            .onEach { articles ->
                val existing = articles.mapTo(HashSet()) { it.url }
                setState {
                    copy(
                        articles = articles,
                        isLoading = false,
                        // Articles removed elsewhere (e.g. from Detail) can't stay selected
                        selectedUrls = selectedUrls.filterTo(HashSet()) { it in existing }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: BookmarkIntent) {
        when (intent) {
            is BookmarkIntent.EnterSelection -> setState { copy(selectedUrls = selectedUrls + intent.url) }
            is BookmarkIntent.ToggleSelection -> setState {
                val updated = if (intent.url in selectedUrls) selectedUrls - intent.url else selectedUrls + intent.url
                copy(selectedUrls = updated)
            }
            BookmarkIntent.ToggleSelectAll -> setState {
                copy(selectedUrls = if (allSelected) emptySet() else articles.mapTo(HashSet()) { it.url })
            }
            BookmarkIntent.ClearSelection -> setState { copy(selectedUrls = emptySet()) }
            BookmarkIntent.DeleteClick -> setState { copy(showDeleteDialog = isSelectionMode) }
            BookmarkIntent.DismissDelete -> setState { copy(showDeleteDialog = false) }
            BookmarkIntent.ConfirmDelete -> deleteSelected()
        }
    }

    private fun deleteSelected() {
        val urls = currentState.selectedUrls
        setState { copy(showDeleteDialog = false) }
        if (urls.isEmpty()) return

        viewModelScope.launch {
            try {
                deleteBookmarks(urls)
                setState { copy(selectedUrls = emptySet()) }
                sendEffect(BookmarkEffect.ShowDeleted(urls.size))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendEffect(BookmarkEffect.ShowDeleteFailed)
            }
        }
    }
}
