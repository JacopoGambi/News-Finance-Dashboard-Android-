package com.example.newsfinance.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CryptoDtoMapperTest {

    private fun dto(
        id: String? = "bitcoin",
        symbol: String? = "btc",
        name: String? = "Bitcoin"
    ) = CryptoDto(
        id = id,
        symbol = symbol,
        name = name,
        image = "https://img/btc.png",
        currentPrice = 50000.0,
        marketCap = 1_000_000.0,
        marketCapRank = 1,
        priceChangePercentage24h = 2.5,
        lastUpdated = "2024-01-01T00:00:00Z"
    )

    @Test
    fun `valid dto maps to domain and uppercases symbol`() {
        val crypto = dto().toDomain()
        assertEquals("bitcoin", crypto?.id)
        assertEquals("BTC", crypto?.symbol)
        assertEquals("Bitcoin", crypto?.name)
        assertEquals(50000.0, crypto?.currentPrice!!, 0.0)
    }

    @Test
    fun `null id returns null`() {
        assertNull(dto(id = null).toDomain())
    }

    @Test
    fun `null symbol returns null`() {
        assertNull(dto(symbol = null).toDomain())
    }

    @Test
    fun `null name returns null`() {
        assertNull(dto(name = null).toDomain())
    }
}
