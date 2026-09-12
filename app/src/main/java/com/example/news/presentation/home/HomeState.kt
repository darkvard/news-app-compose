package com.example.news.presentation.home

data class HomeState(
    val newTicker: String = "",
    val isLoading: Boolean = false
)