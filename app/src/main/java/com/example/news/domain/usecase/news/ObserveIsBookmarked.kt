package com.example.news.domain.usecase.news

import com.example.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIsBookmarked @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(url: String): Flow<Boolean> = newsRepository.observeIsBookmarked(url)
}
