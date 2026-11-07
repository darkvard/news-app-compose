package com.example.news.domain.model

/**
 * Errors the data layer can surface. Lets the presentation layer show the right state
 * without knowing about Retrofit / OkHttp exception types.
 */
sealed class DataError(cause: Throwable? = null) : Exception(cause) {
    /** No internet / host unreachable. */
    class NoConnection(cause: Throwable? = null) : DataError(cause)

    /** The server took too long to answer. */
    class Timeout(cause: Throwable? = null) : DataError(cause)

    /** API quota or the free-plan result limit was hit (HTTP 426 / 429). */
    class RateLimited(cause: Throwable? = null) : DataError(cause)

    /** Any other non-2xx HTTP response. */
    class Server(val code: Int, cause: Throwable? = null) : DataError(cause)

    class Unknown(cause: Throwable? = null) : DataError(cause)
}
