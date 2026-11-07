package com.example.news.domain.usecase.news

import androidx.paging.PagingData
import com.example.news.domain.model.Article
import com.example.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNews @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Article>> =
        newsRepository.searchNews(query.trim())
}
