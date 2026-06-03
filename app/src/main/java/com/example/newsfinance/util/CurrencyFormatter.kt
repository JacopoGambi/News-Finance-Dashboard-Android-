package com.example.newsfinance.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formattazione centralizzata dei prezzi in valuta.
 * Mappa la valuta scelta dall'utente al Locale corretto per simbolo e separatori,
 * così la stessa logica è usata da UI e notifiche.
 */
object CurrencyFormatter {

    fun format(price: Double?, currency: String): String {
        if (price == null) return "N/A"
        return NumberFormat.getCurrencyInstance(localeFor(currency)).format(price)
    }

    private fun localeFor(currency: String): Locale =
        if (currency.equals("eur", ignoreCase = true)) Locale.GERMANY else Locale.US
}
