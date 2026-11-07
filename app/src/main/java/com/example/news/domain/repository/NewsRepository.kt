package com.example.news.domain.repository

import androidx.paging.PagingData
import com.example.news.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    /** Home feed. Backed by the offline cache, refreshed from the network. */
    fun getNews(): Flow<PagingData<Article>>

    /** Network-only search. */
    fun searchNews(query: String): Flow<PagingData<Article>>

    /** Saved articles, newest first. Emits again whenever bookmarks change. */
    fun observeBookmarks(): Flow<List<Article>>

    fun observeIsBookmarked(url: String): Flow<Boolean>

    /** Saves the article if it isn't bookmarked yet, removes it otherwise. Returns the new state. */
    suspend fun toggleBookmark(article: Article): Boolean

    suspend fun deleteBookmarks(urls: Set<String>)
}
