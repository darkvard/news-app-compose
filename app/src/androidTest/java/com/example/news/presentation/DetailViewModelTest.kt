package com.example.news.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.news.R
import com.example.news.domain.model.Article
import com.example.news.domain.repository.NewsRepository
import com.example.news.domain.usecase.news.ObserveIsBookmarked
import com.example.news.domain.usecase.news.ToggleBookmark
import com.example.news.presentation.detail.DetailEffect
import com.example.news.presentation.detail.DetailIntent
import com.example.news.presentation.detail.DetailViewModel
import com.example.news.presentation.navgraph.ArticleNavArg
import com.example.news.presentation.navgraph.toNavArg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented because `SavedStateHandle.toRoute` (type-safe nav args) needs a real Android Bundle.
 */
@RunWith(AndroidJUnit4::class)
class DetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val article = Article(url = "https://bbc.com/a", title = "A")
    private val repository = InMemoryBookmarks()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    /** Same shape Navigation stores the argument in: the NavType's JSON string under the property name. */
    private fun createViewModel() = DetailViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf("article" to Json.encodeToString(ArticleNavArg.serializer(), article.toNavArg()))
        ),
        observeIsBookmarked = ObserveIsBookmarked(repository),
        toggleBookmark = ToggleBookmark(repository)
    )

    @Test
    fun readsTheArticleFromTheNavArgument() {
        assertEquals(article, createViewModel().state.value.article)
    }

    @Test
    fun toggleBookmark_updatesStateAndShowsMessage() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isBookmarked)

        viewModel.effects.test {
            viewModel.onIntent(DetailIntent.ToggleBookmark)
            assertEquals(DetailEffect.ShowMessage(R.string.bookmark_saved), awaitItem())
            advanceUntilIdle()
            assertTrue(viewModel.state.value.isBookmarked)

            viewModel.onIntent(DetailIntent.ToggleBookmark)
            assertEquals(DetailEffect.ShowMessage(R.string.bookmark_removed), awaitItem())
            advanceUntilIdle()
            assertFalse(viewModel.state.value.isBookmarked)
        }
    }

    @Test
    fun shareAndOpen_areSentAsEffectsWithTheArticleUrl() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.effects.test {
            viewModel.onIntent(DetailIntent.Share)
            assertEquals(DetailEffect.ShareUrl(article.url), awaitItem())
            viewModel.onIntent(DetailIntent.OpenInBrowser)
            assertEquals(DetailEffect.OpenUrl(article.url), awaitItem())
        }
    }

    private class InMemoryBookmarks : NewsRepository {
        private val urls = MutableStateFlow<Set<String>>(emptySet())
        override fun getNews(): Flow<PagingData<Article>> = emptyFlow()
        override fun searchNews(query: String): Flow<PagingData<Article>> = emptyFlow()
        override fun observeBookmarks(): Flow<List<Article>> = emptyFlow()
        override fun observeIsBookmarked(url: String) = urls.map { url in it }
        override suspend fun toggleBookmark(article: Article): Boolean {
            val saved = article.url !in urls.value
            urls.value = if (saved) urls.value + article.url else urls.value - article.url
            return saved
        }
        override suspend fun deleteBookmarks(urls: Set<String>) = Unit
    }
}
