package com.example.newsfinance.data.remote.dto

import com.example.newsfinance.domain.model.Crypto
import com.google.gson.annotations.SerializedName

data class CryptoDto(
    @SerializedName("id") val id: String?,
    @SerializedName("symbol") val symbol: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("current_price") val currentPrice: Double?,
    @SerializedName("market_cap") val marketCap: Double?,
    @SerializedName("market_cap_rank") val marketCapRank: Int?,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double?,
    @SerializedName("last_updated") val lastUpdated: String?
)

fun CryptoDto.toDomain(): Crypto? {
    val safeId = id ?: return null
    val safeSymbol = symbol ?: return null
    val safeName = name ?: return null
    return Crypto(
        id = safeId,
        symbol = safeSymbol.uppercase(),
        name = safeName,
        imageUrl = image,
        currentPrice = currentPrice,
        marketCap = marketCap,
        marketCapRank = marketCapRank,
        priceChangePercentage24h = priceChangePercentage24h,
        lastUpdated = lastUpdated
    )
}
