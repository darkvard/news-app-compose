package com.example.news.presentation.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.news.usecase.news.GetNews
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val newsUseCase: GetNews
) : ViewModel() {

    val state = mutableStateOf(HomeState())


    val news = newsUseCase(
        sources = listOf("bbc-news", "abc-news", "google-news")
    ).cachedIn(viewModelScope)


}