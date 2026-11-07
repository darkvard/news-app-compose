package com.example.news.presentation

import app.cash.turbine.test
import com.example.news.domain.model.Article
import com.example.news.domain.usecase.news.DeleteBookmarks
import com.example.news.domain.usecase.news.ObserveBookmarks
import com.example.news.fake.FakeNewsRepository
import com.example.news.presentation.bookmark.BookmarkEffect
import com.example.news.presentation.bookmark.BookmarkIntent
import com.example.news.presentation.bookmark.BookmarkState
import com.example.news.presentation.bookmark.BookmarkViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BookmarkViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val a = Article(url = "https://bbc.com/a", title = "A")
    private val b = Article(url = "https://bbc.com/b", title = "B")
    private val c = Article(url = "https://bbc.com/c", title = "C")

    private lateinit var repository: FakeNewsRepository
    private lateinit var viewModel: BookmarkViewModel

    @Before
    fun setUp() {
        repository = FakeNewsRepository()
        viewModel = BookmarkViewModel(ObserveBookmarks(repository), DeleteBookmarks(repository))
    }

    /** Keeps `state` subscribed (WhileSubscribed) and lets the flows settle. */
    private fun TestScope.startCollecting() {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
    }

    private val state get() = viewModel.state.value

    @Test
    fun `state follows bookmark changes`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.state.test {
            assertEquals(BookmarkState(articles = emptyList(), isLoading = false), awaitItem())

            repository.toggleBookmark(a)
            assertEquals(listOf(a), awaitItem().articles)

            repository.toggleBookmark(a)
            assertEquals(emptyList<Article>(), awaitItem().articles)
        }
    }

    @Test
    fun `long press enters selection mode and tapping toggles items`() = runTest(mainDispatcherRule.dispatcher) {
        repository.bookmarks.value = listOf(a, b, c)
        startCollecting()

        viewModel.onIntent(BookmarkIntent.EnterSelection(a.url))
        advanceUntilIdle()
        assertTrue(state.isSelectionMode)
        assertEquals(setOf(a.url), state.selectedUrls)

        viewModel.onIntent(BookmarkIntent.ToggleSelection(b.url))
        advanceUntilIdle()
        assertEquals(setOf(a.url, b.url), state.selectedUrls)

        // Deselecting everything leaves selection mode
        viewModel.onIntent(BookmarkIntent.ToggleSelection(a.url))
        viewModel.onIntent(BookmarkIntent.ToggleSelection(b.url))
        advanceUntilIdle()
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `select all toggles between all and none`() = runTest(mainDispatcherRule.dispatcher) {
        repository.bookmarks.value = listOf(a, b, c)
        startCollecting()
        viewModel.onIntent(BookmarkIntent.EnterSelection(a.url))

        viewModel.onIntent(BookmarkIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertTrue(state.allSelected)

        viewModel.onIntent(BookmarkIntent.ToggleSelectAll)
        advanceUntilIdle()
        assertEquals(emptySet<String>(), state.selectedUrls)
    }

    @Test
    fun `delete asks for confirmation then deletes only the selected articles`() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.bookmarks.value = listOf(a, b, c)
            startCollecting()
            viewModel.onIntent(BookmarkIntent.EnterSelection(a.url))
            viewModel.onIntent(BookmarkIntent.ToggleSelection(c.url))

            viewModel.effects.test {
                viewModel.onIntent(BookmarkIntent.DeleteClick)
                advanceUntilIdle()
                assertTrue(state.showDeleteDialog)
                assertEquals(emptyList<Set<String>>(), repository.deletedUrls) // nothing deleted yet

                viewModel.onIntent(BookmarkIntent.ConfirmDelete)
                assertEquals(BookmarkEffect.ShowDeleted(count = 2), awaitItem())
            }
            advanceUntilIdle()

            assertEquals(listOf(setOf(a.url, c.url)), repository.deletedUrls)
            assertEquals(listOf(b), state.articles)
            assertFalse(state.isSelectionMode)
            assertFalse(state.showDeleteDialog)
        }

    @Test
    fun `dismissing the dialog keeps everything`() = runTest(mainDispatcherRule.dispatcher) {
        repository.bookmarks.value = listOf(a, b)
        startCollecting()
        viewModel.onIntent(BookmarkIntent.EnterSelection(a.url))
        viewModel.onIntent(BookmarkIntent.DeleteClick)

        viewModel.onIntent(BookmarkIntent.DismissDelete)
        advanceUntilIdle()

        assertFalse(state.showDeleteDialog)
        assertEquals(setOf(a.url), state.selectedUrls)
        assertEquals(emptyList<Set<String>>(), repository.deletedUrls)
    }

    @Test
    fun `articles removed elsewhere are dropped from the selection`() = runTest(mainDispatcherRule.dispatcher) {
        repository.bookmarks.value = listOf(a, b)
        startCollecting()
        viewModel.onIntent(BookmarkIntent.EnterSelection(a.url))
        viewModel.onIntent(BookmarkIntent.ToggleSelection(b.url))

        repository.toggleBookmark(a) // e.g. un-bookmarked from the Detail screen
        advanceUntilIdle()

        assertEquals(setOf(b.url), state.selectedUrls)
    }
}
