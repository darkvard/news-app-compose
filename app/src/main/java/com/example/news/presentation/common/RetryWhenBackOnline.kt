package com.example.news.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

/** When the connection comes back, retry whatever page load failed while offline. */
@Composable
fun <T : Any> RetryWhenBackOnline(articles: LazyPagingItems<T>, isOnline: Boolean) {
    LaunchedEffect(isOnline) {
        val loadState = articles.loadState
        val hasError = loadState.refresh is LoadState.Error || loadState.append is LoadState.Error
        if (isOnline && hasError) articles.retry()
    }
}
