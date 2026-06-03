package com.example.newsfinance.util

import com.example.newsfinance.BuildConfig

object Constants {

    val GNEWS_API_KEY: String       get() = BuildConfig.GNEWS_API_KEY
    val COINGECKO_API_KEY: String   get() = BuildConfig.COINGECKO_API_KEY
    val GNEWS_API_BASE_URL: String  get() = BuildConfig.GNEWS_API_BASE_URL
    val COINGECKO_BASE_URL: String  get() = BuildConfig.COINGECKO_BASE_URL

    const val DATABASE_NAME: String = "newsfinance.db"

    const val NOTIFICATION_CHANNEL_PRICE_ALERTS: String = "price_alerts"
    const val WORKER_PRICE_ALERTS_TAG: String = "price_alert_worker"

    const val DEFAULT_VS_CURRENCY: String = "usd"
    const val DEFAULT_COUNTRY: String = "it"
    const val DEFAULT_LANG: String = "it"
    const val DEFAULT_NEWS_MAX: Int = 25
}
