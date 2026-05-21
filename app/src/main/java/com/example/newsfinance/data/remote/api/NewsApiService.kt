package com.example.newsfinance.data.remote.api

import com.example.newsfinance.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoint NewsAPI v2.
 * Base URL gestita da Retrofit (vedi NetworkModule).
 */
interface NewsApiService {

    /**
     * Top headlines per paese e (opzionale) categoria.
     */
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String,
        @Query("category") category: String?,
        @Query("apiKey") apiKey: String
    ): NewsResponseDto

    /**
     * Ricerca articoli per keyword (endpoint /everything).
     */
    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): NewsResponseDto
}
