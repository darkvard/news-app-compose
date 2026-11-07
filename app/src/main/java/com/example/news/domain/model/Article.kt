package com.example.news.domain.model

/**
 * Pure domain model (no Android / framework annotations).
 * [url] is the article's identity: bookmarks and the offline cache are keyed by it.
 */
data class Article(
    val url: String,
    val title: String? = null,
    val author: String? = null,
    val description: String? = null,
    val content: String? = null,
    val publishedAt: String? = null,
    val source: Source? = null,
    val urlToImage: String? = null
)
