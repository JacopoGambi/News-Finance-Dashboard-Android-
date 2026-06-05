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

    /** Formato compatto per valori grandi (es. "$2.41T"), usato nella hero della Home. */
    fun formatCompact(value: Double?, currency: String): String {
        if (value == null) return "N/A"
        val symbol = if (currency.equals("eur", ignoreCase = true)) "€" else "$"
        val abs = kotlin.math.abs(value)
        return when {
            abs >= 1e12 -> "$symbol%.2fT".format(value / 1e12)
            abs >= 1e9 -> "$symbol%.2fB".format(value / 1e9)
            abs >= 1e6 -> "$symbol%.2fM".format(value / 1e6)
            else -> format(value, currency)
        }
    }

    private fun localeFor(currency: String): Locale =
        if (currency.equals("eur", ignoreCase = true)) Locale.GERMANY else Locale.US
}
