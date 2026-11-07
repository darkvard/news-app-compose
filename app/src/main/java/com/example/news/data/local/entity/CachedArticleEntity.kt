package com.example.news.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Home feed cache. Kept separate from bookmarks so cache clears never touch saved articles. */
@Entity(tableName = "cached_articles")
data class CachedArticleEntity(
    @PrimaryKey val url: String,
    val title: String?,
    val author: String?,
    val description: String?,
    val content: String?,
    val publishedAt: String?,
    val sourceId: String?,
    val sourceName: String?,
    val urlToImage: String?,
    /** API page this article came from + its index on that page: keeps the feed order. */
    val page: Int,
    val position: Int,
    val cachedAt: Long
)
