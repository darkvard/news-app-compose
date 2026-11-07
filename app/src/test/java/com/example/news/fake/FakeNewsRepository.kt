package com.example.news.fake

import androidx.paging.PagingData
import com.example.news.domain.model.Article
import com.example.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakeNewsRepository : NewsRepository {

    val searchedQueries = mutableListOf<String>()
    val bookmarks = MutableStateFlow<List<Article>>(emptyList())

    override fun getNews(): Flow<PagingData<Article>> = flowOf(PagingData.empty())

    override fun searchNews(query: String): Flow<PagingData<Article>> {
        searchedQueries += query
        return flowOf(PagingData.empty())
    }

    override fun observeBookmarks(): Flow<List<Article>> = bookmarks

    override fun observeIsBookmarked(url: String): Flow<Boolean> =
        bookmarks.map { list -> list.any { it.url == url } }

    override suspend fun toggleBookmark(article: Article): Boolean {
        val isBookmarked = bookmarks.value.any { it.url == article.url }
        bookmarks.value = if (isBookmarked) {
            bookmarks.value.filterNot { it.url == article.url }
        } else {
            listOf(article) + bookmarks.value
        }
        return !isBookmarked
    }

    val deletedUrls = mutableListOf<Set<String>>()

    override suspend fun deleteBookmarks(urls: Set<String>) {
        deletedUrls += urls
        bookmarks.value = bookmarks.value.filterNot { it.url in urls }
    }
}
