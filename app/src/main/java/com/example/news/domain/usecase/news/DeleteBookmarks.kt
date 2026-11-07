package com.example.news.domain.usecase.news

import com.example.news.domain.repository.NewsRepository
import javax.inject.Inject

class DeleteBookmarks @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(urls: Set<String>) = newsRepository.deleteBookmarks(urls)
}
