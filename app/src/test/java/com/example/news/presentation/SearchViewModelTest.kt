package com.example.news.presentation

import com.example.news.domain.usecase.network.ObserveNetworkStatus
import com.example.news.domain.usecase.news.SearchNews
import com.example.news.fake.FakeNetworkMonitor
import com.example.news.fake.FakeNewsRepository
import com.example.news.presentation.search.SearchIntent
import com.example.news.presentation.search.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeNewsRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        repository = FakeNewsRepository()
        viewModel = SearchViewModel(SearchNews(repository), ObserveNetworkStatus(FakeNetworkMonitor()))
    }

    @Test
    fun `nothing is searched while the query is blank`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.articles.collect {} }
        advanceUntilIdle()

        assertEquals(emptyList<String>(), repository.searchedQueries)
        assertEquals("", viewModel.state.value.submittedQuery)
    }

    @Test
    fun `typing searches once after the debounce with the last trimmed query`() =
        runTest(mainDispatcherRule.dispatcher) {
            backgroundScope.launch { viewModel.articles.collect {} }

            viewModel.onIntent(SearchIntent.QueryChanged("bit"))
            advanceTimeBy(100)
            viewModel.onIntent(SearchIntent.QueryChanged("bitcoin "))
            advanceUntilIdle()

            assertEquals("bitcoin ", viewModel.query)
            assertEquals(listOf("bitcoin"), repository.searchedQueries)
        }

    @Test
    fun `keyboard search runs immediately and the debounce does not repeat it`() =
        runTest(mainDispatcherRule.dispatcher) {
            backgroundScope.launch { viewModel.articles.collect {} }

            viewModel.onIntent(SearchIntent.QueryChanged("nasa"))
            viewModel.onIntent(SearchIntent.Submit)
            runCurrent()
            assertEquals(listOf("nasa"), repository.searchedQueries)

            advanceUntilIdle()
            assertEquals(listOf("nasa"), repository.searchedQueries)
        }

    @Test
    fun `clearing the query clears results right away`() = runTest(mainDispatcherRule.dispatcher) {
        backgroundScope.launch { viewModel.articles.collect {} }
        viewModel.onIntent(SearchIntent.QueryChanged("nasa"))
        advanceUntilIdle()
        assertEquals("nasa", viewModel.state.value.submittedQuery)

        viewModel.onIntent(SearchIntent.QueryChanged(""))
        runCurrent()

        assertEquals("", viewModel.state.value.submittedQuery)
    }
}
