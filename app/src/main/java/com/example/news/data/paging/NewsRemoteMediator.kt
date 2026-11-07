package com.example.news.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.news.data.local.NewsDatabase
import com.example.news.data.local.entity.CachedArticleEntity
import com.example.news.data.local.entity.RemoteKeyEntity
import com.example.news.data.mapper.toCachedEntity
import com.example.news.data.mapper.toDomainOrNull
import com.example.news.data.remote.NewsApi
import com.example.news.data.remote.NewsApiConfig
import com.example.news.data.remote.isEndOfPagination
import com.example.news.data.remote.toDataError
import kotlin.coroutines.cancellation.CancellationException

/**
 * Offline-first home feed: the UI only ever reads from Room ([CachedArticleDao.pagingSource]);
 * this mediator fills Room from the network when the cache is stale or the user scrolls to the end.
 */
@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val newsApi: NewsApi,
    private val database: NewsDatabase,
    private val now: () -> Long = System::currentTimeMillis
) : RemoteMediator<Int, CachedArticleEntity>() {

    private val articleDao = database.cachedArticleDao
    private val remoteKeyDao = database.remoteKeyDao

    override suspend fun initialize(): InitializeAction {
        val lastRefresh = articleDao.lastRefreshTime() ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        // Fresh cache: show it immediately (works offline) without hitting the network.
        return if (now() - lastRefresh < CACHE_TIMEOUT_MILLIS) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CachedArticleEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> STARTING_PAGE
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> remoteKeyDao.get(FEED)?.nextPage
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val pageSize = NewsApiConfig.PAGE_SIZE
            val response = newsApi.getNews(sources = NewsApiConfig.SOURCES, page = page, pageSize = pageSize)
            val endReached = isEndOfPagination(
                page = page,
                pageSize = pageSize,
                receivedCount = response.articles.size,
                totalResults = response.totalResults,
                maxResults = NewsApiConfig.MAX_RESULTS
            )
            val cachedAt = now()
            val entities = response.articles
                .mapNotNull { it.toDomainOrNull() }
                .mapIndexed { index, article ->
                    article.toCachedEntity(page = page, position = index, cachedAt = cachedAt)
                }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    articleDao.clearAll()
                    remoteKeyDao.delete(FEED)
                }
                remoteKeyDao.upsert(RemoteKeyEntity(feed = FEED, nextPage = if (endReached) null else page + 1))
                articleDao.insertAll(entities)
            }
            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            MediatorResult.Error(e.toDataError())
        }
    }

    private companion object {
        const val FEED = "home"
        const val STARTING_PAGE = 1

        /** How long the cached feed is considered fresh before refreshing on launch. */
        const val CACHE_TIMEOUT_MILLIS = 60 * 60 * 1000L
    }
}
