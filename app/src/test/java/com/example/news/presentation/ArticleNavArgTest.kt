package com.example.news.presentation

import com.example.news.domain.model.Article
import com.example.news.domain.model.Source
import com.example.news.presentation.navgraph.toArticle
import com.example.news.presentation.navgraph.toNavArg
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleNavArgTest {

    @Test
    fun `nav arg round trip keeps every field`() {
        val article = Article(
            url = "https://bbc.com/a",
            title = "Title",
            author = "Jane",
            description = "Desc",
            content = "Body",
            publishedAt = "2023-06-16T22:24:33Z",
            source = Source(id = "bbc-news", name = "BBC News"),
            urlToImage = "https://bbc.com/a.jpg"
        )
        assertEquals(article, article.toNavArg().toArticle())
    }

    @Test
    fun `missing source stays null`() {
        val article = Article(url = "https://bbc.com/a")
        assertEquals(null, article.toNavArg().toArticle().source)
    }
}
