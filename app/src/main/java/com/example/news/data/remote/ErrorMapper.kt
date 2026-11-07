package com.example.news.data.remote

import com.example.news.domain.model.DataError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Converts low-level exceptions into [DataError] so upper layers never see Retrofit/OkHttp types. */
fun Throwable.toDataError(): DataError = when (this) {
    is DataError -> this
    is SocketTimeoutException -> DataError.Timeout(this)
    is IOException -> DataError.NoConnection(this) // UnknownHost, Connect, NoRouteToHost...
    is HttpException -> when (code()) {
        426, 429 -> DataError.RateLimited(this)
        else -> DataError.Server(code(), this)
    }
    else -> DataError.Unknown(this)
}

/** True when the API says there are no more pages to load. */
fun isEndOfPagination(page: Int, pageSize: Int, receivedCount: Int, totalResults: Int, maxResults: Int): Boolean =
    receivedCount == 0 || page * pageSize >= minOf(totalResults, maxResults)
