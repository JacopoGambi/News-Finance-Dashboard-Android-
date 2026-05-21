package com.example.newsfinance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.newsfinance.domain.model.Crypto

/**
 * Entità Room per le crypto in watchlist.
 * @property alertThreshold soglia opzionale per la notifica di superamento prezzo.
 */
@Entity(tableName = "cryptos")
data class CryptoEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val currentPrice: Double?,
    val marketCap: Double?,
    val marketCapRank: Int?,
    val priceChangePercentage24h: Double?,
    val lastUpdated: String?,
    val alertThreshold: Double?
)

fun CryptoEntity.toDomain(): Crypto = Crypto(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    priceChangePercentage24h = priceChangePercentage24h,
    lastUpdated = lastUpdated,
    alertThreshold = alertThreshold
)

fun Crypto.toEntity(): CryptoEntity = CryptoEntity(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice,
    marketCap = marketCap,
    marketCapRank = marketCapRank,
    priceChangePercentage24h = priceChangePercentage24h,
    lastUpdated = lastUpdated,
    alertThreshold = alertThreshold
)
