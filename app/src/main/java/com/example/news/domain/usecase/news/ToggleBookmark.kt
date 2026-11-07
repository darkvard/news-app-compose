package com.example.news.domain.usecase.news

import com.example.news.domain.model.Article
import com.example.news.domain.repository.NewsRepository
import javax.inject.Inject

class ToggleBookmark @Inject constructor(
    private val newsRepository: NewsRepository
) {
    /** @return true if the article is bookmarked after the call. */
    suspend operator fun invoke(article: Article): Boolean = newsRepository.toggleBookmark(article)
}
