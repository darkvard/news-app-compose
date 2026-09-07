package com.example.news.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.news.usecase.news.NewsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val newsUseCase: NewsUseCases
) : ViewModel() {


    val news = newsUseCase.getNews(
        sources = listOf("bbc-news", "abc-news", "google-news")
    ).cachedIn(viewModelScope)

}