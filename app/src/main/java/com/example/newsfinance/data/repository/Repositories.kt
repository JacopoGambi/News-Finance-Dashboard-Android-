package com.example.newsfinance.data.repository

import com.example.newsfinance.data.local.dao.ArticleDao
import com.example.newsfinance.data.local.dao.CryptoDao
import com.example.newsfinance.data.local.entity.toDomain
import com.example.newsfinance.data.local.entity.toEntity
import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.FavoritesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementazione di FavoritesRepository basata sui DAO Room.
 * Le operazioni di scrittura usano Dispatchers.IO per non bloccare il main thread.
 * Le query reattive ritornano Flow e vengono mappate da entity a modello di dominio.
 */
@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val cryptoDao: CryptoDao
) : FavoritesRepository {

    override fun getFavoriteArticles(): Flow<List<Article>> =
        articleDao.getAllFavoriteArticles()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun addFavoriteArticle(article: Article) = withContext(Dispatchers.IO) {
        articleDao.insertArticle(article.toEntity())
    }

    override suspend fun removeFavoriteArticle(article: Article) = withContext(Dispatchers.IO) {
        articleDao.deleteArticle(article.toEntity())
    }

    override fun isArticleFavorite(url: String): Flow<Boolean> =
        articleDao.isArticleFavorite(url)

    override fun getWatchlistCryptos(): Flow<List<Crypto>> =
        cryptoDao.getAllWatchlistCryptos()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun addToWatchlist(crypto: Crypto) = withContext(Dispatchers.IO) {
        cryptoDao.insertCrypto(crypto.toEntity())
    }

    override suspend fun removeFromWatchlist(crypto: Crypto) = withContext(Dispatchers.IO) {
        cryptoDao.deleteCrypto(crypto.toEntity())
    }

    override suspend fun updateCryptoAlertThreshold(id: String, threshold: Double) =
        withContext(Dispatchers.IO) {
            cryptoDao.updateAlertThreshold(id, threshold)
        }

    override suspend fun getCryptosWithAlerts(): List<Crypto> = withContext(Dispatchers.IO) {
        cryptoDao.getCryptosWithAlerts().map { it.toDomain() }
    }
}
