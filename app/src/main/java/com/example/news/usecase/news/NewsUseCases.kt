package com.example.news.usecase.news

class NewsUseCases(
    val getNews: GetNews,
    val searchNews: SearchNews,
    val getArticle: GetArticle,
    val upsertArticle: UpsertArticle,
    val deleteArticle: DeleteArticle
)