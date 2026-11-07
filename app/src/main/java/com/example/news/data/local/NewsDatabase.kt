package com.example.news.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.news.data.local.dao.BookmarkDao
import com.example.news.data.local.dao.CachedArticleDao
import com.example.news.data.local.dao.RemoteKeyDao
import com.example.news.data.local.entity.BookmarkEntity
import com.example.news.data.local.entity.CachedArticleEntity
import com.example.news.data.local.entity.RemoteKeyEntity

@Database(
    entities = [BookmarkEntity::class, CachedArticleEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {
    abstract val bookmarkDao: BookmarkDao
    abstract val cachedArticleDao: CachedArticleDao
    abstract val remoteKeyDao: RemoteKeyDao

    companion object {
        const val NAME = "news_db"
    }
}
