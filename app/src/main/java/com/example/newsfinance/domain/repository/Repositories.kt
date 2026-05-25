package com.example.newsfinance.domain.repository

import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.model.Crypto
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

    suspend fun updateCryptoAlertThreshold(id: String, threshold: Double)

    suspend fun getCryptosWithAlerts(): List<Crypto>
}
