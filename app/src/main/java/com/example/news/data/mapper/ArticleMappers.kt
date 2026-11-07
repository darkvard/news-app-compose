package com.example.news.data.mapper

import com.example.news.data.local.entity.BookmarkEntity
import com.example.news.data.local.entity.CachedArticleEntity
import com.example.news.data.remote.dto.ArticleDto
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source

/** NewsAPI replaces taken-down articles with this placeholder. */
private const val REMOVED_PLACEHOLDER = "[Removed]"

/** Returns null for articles we can't use: no url (our identity key) or removed by the publisher. */
fun ArticleDto.toDomainOrNull(): Article? {
    val url = url?.trim()
    if (url.isNullOrEmpty() || url == REMOVED_PLACEHOLDER || title == REMOVED_PLACEHOLDER) return null
    return Article(
        url = url,
        title = title,
        author = author,
        description = description,
        content = content,
        publishedAt = publishedAt,
        source = source?.let { Source(id = it.id, name = it.name) },
        urlToImage = urlToImage
    )
}

fun Article.toCachedEntity(page: Int, position: Int, cachedAt: Long) = CachedArticleEntity(
    url = url,
    title = title,
    author = author,
    description = description,
    content = content,
    publishedAt = publishedAt,
    sourceId = source?.id,
    sourceName = source?.name,
    urlToImage = urlToImage,
    page = page,
    position = position,
    cachedAt = cachedAt
)

fun CachedArticleEntity.toDomain() = Article(
    url = url,
    title = title,
    author = author,
    description = description,
    content = content,
    publishedAt = publishedAt,
    source = toSource(sourceId, sourceName),
    urlToImage = urlToImage
)

fun Article.toBookmarkEntity(savedAt: Long) = BookmarkEntity(
    url = url,
    title = title,
    author = author,
    description = description,
    content = content,
    publishedAt = publishedAt,
    sourceId = source?.id,
    sourceName = source?.name,
    urlToImage = urlToImage,
    savedAt = savedAt
)

fun BookmarkEntity.toDomain() = Article(
    url = url,
    title = title,
    author = author,
    description = description,
    content = content,
    publishedAt = publishedAt,
    source = toSource(sourceId, sourceName),
    urlToImage = urlToImage
)

private fun toSource(id: String?, name: String?): Source? =
    if (id == null && name == null) null else Source(id = id, name = name)
