package com.example.newsfinance.data.local.entity

import com.example.newsfinance.domain.model.CryptoAlert
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertEntityMapperTest {

    @Test
    fun `entity to domain preserves triggered flag`() {
        val entity = AlertEntity(
            id = 7,
            cryptoId = "bitcoin",
            cryptoName = "Bitcoin",
            threshold = 60000.0,
            above = true,
            triggered = true
        )
        val domain = entity.toDomain()
        assertEquals(7L, domain.id)
        assertEquals("bitcoin", domain.cryptoId)
        assertEquals(true, domain.above)
        assertEquals(true, domain.triggered)
    }

    @Test
    fun `domain to entity round trip is stable`() {
        val original = CryptoAlert(
            id = 3,
            cryptoId = "ethereum",
            cryptoName = "Ethereum",
            threshold = 2500.0,
            above = false,
            triggered = false
        )
        assertEquals(original, original.toEntity().toDomain())
    }
}
