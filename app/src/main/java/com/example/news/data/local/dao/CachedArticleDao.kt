package com.example.news.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.news.data.local.entity.CachedArticleEntity

@Dao
interface CachedArticleDao {

    @Query("SELECT * FROM cached_articles ORDER BY page ASC, position ASC")
    fun pagingSource(): PagingSource<Int, CachedArticleEntity>

    /** IGNORE: an article already cached from an earlier page keeps its original position. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(articles: List<CachedArticleEntity>)

    @Query("DELETE FROM cached_articles")
    suspend fun clearAll()

    /** When the current cache was (first) written, i.e. the time of the last full refresh. */
    @Query("SELECT MIN(cachedAt) FROM cached_articles")
    suspend fun lastRefreshTime(): Long?
}
