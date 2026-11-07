package com.example.news.data.remote

import com.example.news.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** The API key is attached by [ApiKeyInterceptor]. */
interface NewsApi {

    @GET("everything")
    suspend fun getNews(
        @Query("sources") sources: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): NewsResponseDto

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("sources") sources: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): NewsResponseDto
}
