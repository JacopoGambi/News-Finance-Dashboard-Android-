package com.example.newsfinance.util

object Constants {

    const val NEWS_API_BASE_URL: String = "https://newsapi.org/v2/"
    const val COINGECKO_BASE_URL: String = "https://api.coingecko.com/api/v3/"
    const val NEWS_API_KEY: String = "50a7762a508d43889047659beacc9ebc"

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
