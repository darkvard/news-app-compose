package com.example.news.usecase.news

import com.example.news.data.local.NewsDao
import com.example.news.domain.model.Article

class UpsertArticle(
    private val dao: NewsDao
) {
    suspend operator fun invoke(article: Article) {
        dao.upsert(article)
    }
}