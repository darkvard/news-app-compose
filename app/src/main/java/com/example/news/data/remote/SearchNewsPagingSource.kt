package com.example.news.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.news.data.mapper.toDomainOrNull
import com.example.news.domain.model.Article
import kotlin.coroutines.cancellation.CancellationException

/** Network-only paging for search results (search results are not cached). */
class SearchNewsPagingSource(
    private val newsApi: NewsApi,
    private val query: String
) : PagingSource<Int, Article>() {

    // The API can return the same article on several pages; LazyColumn keys must be unique.
    private val seenUrls = mutableSetOf<String>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val response = newsApi.searchNews(
                query = query,
                sources = NewsApiConfig.SOURCES,
                page = page,
                pageSize = NewsApiConfig.PAGE_SIZE
            )
            val articles = response.articles
                .mapNotNull { it.toDomainOrNull() }
                .filter { seenUrls.add(it.url) }
            val endReached = isEndOfPagination(
                page = page,
                pageSize = NewsApiConfig.PAGE_SIZE,
                receivedCount = response.articles.size,
                totalResults = response.totalResults,
                maxResults = NewsApiConfig.MAX_RESULTS
            )
            LoadResult.Page(
                data = articles,
                prevKey = null,
                nextKey = if (endReached) null else page + 1
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e.toDataError())
        }
    }

    // Search always restarts from the first page on refresh (seenUrls is per instance).
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? = null

    private companion object {
        const val STARTING_PAGE = 1
    }
}
