package com.example.newsfinance.data.remote.api

import com.example.newsfinance.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoint GNews API v4.
 * Base URL gestita da Retrofit (vedi NetworkModule).
 */
interface NewsApiService {

    /**
     * Top headlines per lingua, paese e (opzionale) categoria.
     */
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String?,
        @Query("lang") lang: String,
        @Query("country") country: String?,
        @Query("max") max: Int,
        @Query("apikey") apiKey: String
    ): NewsResponseDto

    /**
     * Ricerca articoli per keyword.
     */
    @GET("search")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("lang") lang: String,
        @Query("country") country: String,
        @Query("max") max: Int,
        @Query("apikey") apiKey: String
    ): NewsResponseDto
}
