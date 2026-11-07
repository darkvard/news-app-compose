package com.example.news.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.news.R
import com.example.news.domain.usecase.news.ObserveIsBookmarked
import com.example.news.domain.usecase.news.ToggleBookmark
import com.example.news.presentation.base.MviViewModel
import com.example.news.presentation.navgraph.detailsArticle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeIsBookmarked: ObserveIsBookmarked,
    private val toggleBookmark: ToggleBookmark
) : MviViewModel<DetailState, DetailIntent, DetailEffect>(
    // Type-safe nav argument; restored automatically after process death
    DetailState(article = savedStateHandle.detailsArticle())
) {

    init {
        // Straight from Room: correct on entry and updated after every toggle
        observeIsBookmarked(currentState.article.url)
            .onEach { isBookmarked -> setState { copy(isBookmarked = isBookmarked) } }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: DetailIntent) {
        val url = currentState.article.url
        when (intent) {
            DetailIntent.ToggleBookmark -> onToggleBookmark()
            DetailIntent.Share -> sendEffect(DetailEffect.ShareUrl(url))
            DetailIntent.OpenInBrowser -> sendEffect(DetailEffect.OpenUrl(url))
        }
    }

    private fun onToggleBookmark() {
        viewModelScope.launch {
            val message = try {
                if (toggleBookmark(currentState.article)) R.string.bookmark_saved else R.string.bookmark_removed
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                R.string.bookmark_failed
            }
            sendEffect(DetailEffect.ShowMessage(message))
        }
    }
}
