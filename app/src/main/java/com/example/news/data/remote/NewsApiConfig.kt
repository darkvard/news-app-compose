package com.example.news.data.remote

/** NewsAPI settings, only used by the data layer. */
object NewsApiConfig {
    const val BASE_URL = "https://newsapi.org/v2/"

    /** Sources used for both the home feed and search. */
    const val SOURCES = "bbc-news,abc-news,google-news"

    const val PAGE_SIZE = 20

    /**
     * The free "Developer" plan only returns the first 100 results of any query
     * (page 6 of 20 answers HTTP 426 `maximumResultsReached`), so paging stops here.
     */
    const val MAX_RESULTS = 100
}
