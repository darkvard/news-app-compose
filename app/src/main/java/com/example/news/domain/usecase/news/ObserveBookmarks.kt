package com.example.news.domain.usecase.news

import com.example.news.domain.model.Article
import com.example.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBookmarks @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(): Flow<List<Article>> = newsRepository.observeBookmarks()
}
