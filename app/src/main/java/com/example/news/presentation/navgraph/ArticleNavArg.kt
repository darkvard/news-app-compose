package com.example.news.presentation.navgraph

import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import kotlinx.serialization.Serializable

/**
 * Navigation-only copy of [Article]. Serialization is a presentation concern, so the domain model
 * stays free of @Serializable; we map at the navigation boundary instead.
 */
@Serializable
data class ArticleNavArg(
    val url: String,
    val title: String? = null,
    val author: String? = null,
    val description: String? = null,
    val content: String? = null,
    val publishedAt: String? = null,
    val sourceId: String? = null,
    val sourceName: String? = null,
    val urlToImage: String? = null
)

fun Article.toNavArg() = ArticleNavArg(
    url = url,
    title = title,
    author = author,
    description = description,
    content = content,
    publishedAt = publishedAt,
    sourceId = source?.id,
    sourceName = source?.name,
    urlToImage = urlToImage
)

fun ArticleNavArg.toArticle() = Article(
    url = url,
    title = title,
    author = author,
    description = description,
    content = content,
    publishedAt = publishedAt,
    source = if (sourceId == null && sourceName == null) null else Source(id = sourceId, name = sourceName),
    urlToImage = urlToImage
)
