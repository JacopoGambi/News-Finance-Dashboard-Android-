package com.example.newsfinance.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun `null price returns placeholder`() {
        assertEquals("N/A", CurrencyFormatter.format(null, "usd"))
    }

    @Test
    fun `usd formatting uses dollar symbol`() {
        val result = CurrencyFormatter.format(1234.5, "usd")
        assertTrue("atteso simbolo \$ in '$result'", result.contains("$"))
    }

    @Test
    fun `eur formatting uses euro symbol`() {
        val result = CurrencyFormatter.format(1234.5, "eur")
        assertTrue("atteso simbolo € in '$result'", result.contains("€"))
    }

    @Test
    fun `currency code is case insensitive`() {
        val lower = CurrencyFormatter.format(10.0, "eur")
        val upper = CurrencyFormatter.format(10.0, "EUR")
        assertEquals(lower, upper)
    }
}
