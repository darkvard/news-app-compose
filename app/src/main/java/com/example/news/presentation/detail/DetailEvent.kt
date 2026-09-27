package com.example.news.presentation.detail

import com.example.news.domain.model.Article

sealed class DetailEvent {

    data class UpsertDeleteArticle(val article: Article) : DetailEvent()

    object RemoveSideEffect: DetailEvent()

}