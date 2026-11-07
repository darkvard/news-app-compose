package com.example.news.presentation.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.news.R
import com.example.news.domain.model.DataError

/** What to show the user for a given failure. */
data class ErrorUi(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val message: Int,
    val messageArgs: List<Any> = emptyList()
)

fun Throwable.toErrorUi(): ErrorUi = when (this) {
    is DataError.NoConnection -> ErrorUi(
        icon = R.drawable.ic_network_error,
        title = R.string.error_no_connection_title,
        message = R.string.error_no_connection_message
    )
    is DataError.Timeout -> ErrorUi(
        icon = R.drawable.ic_network_error,
        title = R.string.error_timeout_title,
        message = R.string.error_timeout_message
    )
    is DataError.RateLimited -> ErrorUi(
        icon = R.drawable.ic_search_document,
        title = R.string.error_rate_limited_title,
        message = R.string.error_rate_limited_message
    )
    is DataError.Server -> ErrorUi(
        icon = R.drawable.ic_search_document,
        title = R.string.error_generic_title,
        message = R.string.error_server_message,
        messageArgs = listOf(code)
    )
    else -> ErrorUi(
        icon = R.drawable.ic_search_document,
        title = R.string.error_generic_title,
        message = R.string.error_unknown_message
    )
}
