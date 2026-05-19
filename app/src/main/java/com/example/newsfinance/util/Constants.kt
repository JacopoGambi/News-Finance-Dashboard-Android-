package com.example.newsfinance.util

import com.example.newsfinance.BuildConfig

/**
 * Costanti applicative: URL base API e chiavi BuildConfig.
 */
object Constants {

    // Base URL API remote
    const val NEWS_API_BASE_URL: String = BuildConfig.NEWS_API_BASE_URL
    const val COINGECKO_BASE_URL: String = BuildConfig.COINGECKO_BASE_URL

    // Chiave API (iniettata da local.properties via BuildConfig)
    const val NEWS_API_KEY: String = BuildConfig.NEWS_API_KEY

    // Nome database Room
    const val DATABASE_NAME: String = "newsfinance.db"

    // Canale notifiche
    const val NOTIFICATION_CHANNEL_PRICE_ALERTS: String = "price_alerts"

    // Tag worker
    const val WORKER_PRICE_ALERTS_TAG: String = "price_alert_worker"

    // Default valuta crypto
    const val DEFAULT_VS_CURRENCY: String = "usd"

    // Default paese notizie
    const val DEFAULT_COUNTRY: String = "us"
}
