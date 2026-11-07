package com.example.news.data

import com.example.news.data.mapper.toBookmarkEntity
import com.example.news.data.mapper.toCachedEntity
import com.example.news.data.mapper.toDomain
import com.example.news.data.mapper.toDomainOrNull
import com.example.news.data.remote.dto.ArticleDto
import com.example.news.data.remote.dto.SourceDto
import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleMappersTest {

    private val dto = ArticleDto(
        source = SourceDto(id = "bbc-news", name = "BBC News"),
        author = "Jane",
        title = "Title",
        description = "Desc",
        url = " https://bbc.com/a ",
        urlToImage = "https://bbc.com/a.jpg",
        publishedAt = "2023-06-16T22:24:33Z",
        content = "Body"
    )

    @Test
    fun `dto maps to domain with trimmed url`() {
        val article = dto.toDomainOrNull()!!
        assertEquals("https://bbc.com/a", article.url)
        assertEquals(Source(id = "bbc-news", name = "BBC News"), article.source)
        assertEquals("Body", article.content)
    }

    @Test
    fun `articles without url are dropped`() {
        assertNull(dto.copy(url = null).toDomainOrNull())
        assertNull(dto.copy(url = "  ").toDomainOrNull())
    }

    @Test
    fun `removed articles are dropped`() {
        assertNull(dto.copy(url = "[Removed]").toDomainOrNull())
        assertNull(dto.copy(title = "[Removed]").toDomainOrNull())
    }

    @Test
    fun `bookmark entity round trip keeps every field`() {
        val article = dto.toDomainOrNull()!!
        assertEquals(article, article.toBookmarkEntity(savedAt = 1L).toDomain())
    }

    @Test
    fun `cached entity round trip keeps every field`() {
        val article = dto.toDomainOrNull()!!
        assertEquals(article, article.toCachedEntity(page = 2, position = 3, cachedAt = 1L).toDomain())
    }

    @Test
    fun `missing source stays null after round trip`() {
        val article = Article(url = "https://x.com")
        assertNull(article.toBookmarkEntity(savedAt = 1L).toDomain().source)
    }
}
