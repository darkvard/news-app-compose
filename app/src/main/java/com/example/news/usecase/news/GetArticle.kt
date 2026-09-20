package com.example.news.usecase.news

import com.example.news.data.local.NewsDao
import com.example.news.domain.model.Article
import kotlinx.coroutines.flow.Flow

class GetArticle(
    private val dao: NewsDao
) {
    suspend operator fun invoke(): Flow<List<Article>> {
        return dao.getNews()
    }
}