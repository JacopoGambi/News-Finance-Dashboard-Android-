package com.example.newsfinance.domain.model

data class Crypto(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val currentPrice: Double?,
    val marketCap: Double?,
    val marketCapRank: Int?,
    val priceChangePercentage24h: Double?,
    val lastUpdated: String?,
    val alertThreshold: Double? = null
)
