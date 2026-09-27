package com.example.news.presentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.news.domain.model.Article
import com.example.news.usecase.news.DeleteArticle
import com.example.news.usecase.news.GetSavedArticle
import com.example.news.usecase.news.UpsertArticle
import com.example.news.util.UIComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getSavedArticleUseCase: GetSavedArticle,
    private val deleteArticleUseCase: DeleteArticle,
    private val upsertArticleUseCase: UpsertArticle
) : ViewModel() {

    var sideEffect by mutableStateOf<UIComponent?>(null)
        private set

    fun onEvents(event: DetailEvent) {
        when (event) {
            is DetailEvent.UpsertDeleteArticle -> {
                viewModelScope.launch {
                    val article = getSavedArticleUseCase(event.article.url ?: "")
                    if (article == null) {
                        upsertArticle(event.article)
                    } else {
                        deleteArticle(event.article)
                    }
                }
            }

            DetailEvent.RemoveSideEffect -> {
                sideEffect = null
            }
        }
    }

    private suspend fun deleteArticle(article: Article) {
        deleteArticleUseCase(article)
        sideEffect = UIComponent.Toast("Article deleted")
    }

    private suspend fun upsertArticle(article: Article) {
        upsertArticleUseCase(article)
        sideEffect = UIComponent.Toast("Article inserted")
    }
}