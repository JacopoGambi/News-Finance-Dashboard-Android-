package com.example.newsfinance.data.remote.api

import com.example.newsfinance.data.remote.dto.CryptoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoService {

    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") vsCurrency: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): List<CryptoDto>

    @GET("coins/markets")
    suspend fun getMarketsByIds(
        @Query("vs_currency") vsCurrency: String,
        @Query("ids") ids: String
    ): List<CryptoDto>
}
