package com.example.newsfinance.domain.repository

import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.model.CryptoAlert
import kotlinx.coroutines.flow.Flow

/**
 * Repository per la gestione di articoli preferiti e crypto in watchlist.
 * Sorgente unica per i dati salvati in locale tramite Room.
 */
interface FavoritesRepository {

    fun getFavoriteArticles(): Flow<List<Article>>

    suspend fun addFavoriteArticle(article: Article)

    suspend fun removeFavoriteArticle(article: Article)

    fun isArticleFavorite(url: String): Flow<Boolean>

    fun getWatchlistCryptos(): Flow<List<Crypto>>

    suspend fun addToWatchlist(crypto: Crypto)

    suspend fun removeFromWatchlist(crypto: Crypto)
}

/**
 * Repository per gli alert di prezzo. Ogni crypto può avere più alert
 * (al rialzo o al ribasso), indipendenti dalla watchlist.
 */
interface AlertRepository {

    fun getAlertsForCrypto(cryptoId: String): Flow<List<CryptoAlert>>

    suspend fun addAlert(cryptoId: String, cryptoName: String, threshold: Double, above: Boolean)

    suspend fun removeAlert(id: Long)

    suspend fun getAllAlerts(): List<CryptoAlert>
}
