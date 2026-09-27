package com.example.news.usecase.news

import com.example.news.data.local.NewsDao
import com.example.news.domain.model.Article
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedArticles @Inject constructor(
    private val dao: NewsDao
) {
    operator fun invoke(): Flow<List<Article>> {
        return dao.getNews()
    }
}