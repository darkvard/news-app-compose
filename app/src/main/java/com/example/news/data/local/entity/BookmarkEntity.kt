package com.example.news.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val url: String,
    val title: String?,
    val author: String?,
    val description: String?,
    val content: String?,
    val publishedAt: String?,
    val sourceId: String?,
    val sourceName: String?,
    val urlToImage: String?,
    val savedAt: Long
)
