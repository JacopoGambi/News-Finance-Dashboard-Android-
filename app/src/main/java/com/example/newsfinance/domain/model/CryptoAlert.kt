package com.example.newsfinance.domain.model

/**
 * Alert di prezzo impostato su una crypto.
 * @property above se true notifica quando il prezzo sale sopra la soglia,
 *   se false quando scende sotto la soglia.
 */
data class CryptoAlert(
    val id: Long = 0,
    val cryptoId: String,
    val cryptoName: String,
    val threshold: Double,
    val above: Boolean,
    /** True se la soglia è già stata superata e notificata (anti-spam notifiche). */
    val triggered: Boolean = false
)
