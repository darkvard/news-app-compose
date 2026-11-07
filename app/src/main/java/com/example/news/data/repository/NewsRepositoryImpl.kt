package com.example.news.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.news.data.local.NewsDatabase
import com.example.news.data.mapper.toBookmarkEntity
import com.example.news.data.mapper.toDomain
import com.example.news.data.paging.NewsRemoteMediator
import com.example.news.data.remote.NewsApi
import com.example.news.data.remote.NewsApiConfig
import com.example.news.data.remote.SearchNewsPagingSource
import com.example.news.domain.model.Article
import com.example.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val newsApi: NewsApi,
    private val database: NewsDatabase
) : NewsRepository {

    private val bookmarkDao = database.bookmarkDao
    private val pagingConfig = PagingConfig(
        pageSize = NewsApiConfig.PAGE_SIZE,
        initialLoadSize = NewsApiConfig.PAGE_SIZE,
        enablePlaceholders = false
    )

    @OptIn(ExperimentalPagingApi::class)
    override fun getNews(): Flow<PagingData<Article>> = Pager(
        config = pagingConfig,
        remoteMediator = NewsRemoteMediator(newsApi = newsApi, database = database),
        pagingSourceFactory = { database.cachedArticleDao.pagingSource() }
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun searchNews(query: String): Flow<PagingData<Article>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = {
            SearchNewsPagingSource(newsApi = newsApi, query = query)
        }
    ).flow

    override fun observeBookmarks(): Flow<List<Article>> =
        bookmarkDao.observeAll().map { bookmarks -> bookmarks.map { it.toDomain() } }

    override fun observeIsBookmarked(url: String): Flow<Boolean> =
        bookmarkDao.observeIsBookmarked(url)

    override suspend fun toggleBookmark(article: Article): Boolean = database.withTransaction {
        if (bookmarkDao.isBookmarked(article.url)) {
            bookmarkDao.deleteByUrl(article.url)
            false
        } else {
            bookmarkDao.upsert(article.toBookmarkEntity(savedAt = System.currentTimeMillis()))
            true
        }
    }

    override suspend fun deleteBookmarks(urls: Set<String>) {
        if (urls.isNotEmpty()) bookmarkDao.deleteByUrls(urls.toList())
    }
}
