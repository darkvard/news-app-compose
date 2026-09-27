package com.example.news.usecase.news

class NewsUseCases(
    val getNews: GetNews,
    val searchNews: SearchNews,
    val getArticle: GetArticle,
    val getArticles: GetArticles,
    val upsertArticle: UpsertArticle,
    val deleteArticle: DeleteArticle
)