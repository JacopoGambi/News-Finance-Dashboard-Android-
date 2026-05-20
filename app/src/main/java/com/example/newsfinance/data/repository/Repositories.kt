package com.example.newsfinance.data.repository

import com.example.newsfinance.data.local.dao.ArticleDao
import com.example.newsfinance.data.local.dao.CryptoDao
import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.data.remote.api.NewsApiService
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.domain.repository.FavoritesRepository
import com.example.newsfinance.domain.repository.NewsRepository
import javax.inject.Inject

/**
 * Implementazione di NewsRepository.
 * Logica e metodi definiti nello Step 6.
 */
class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService
) : NewsRepository

/**
 * Implementazione di CryptoRepository.
 * Logica e metodi definiti nello Step 6.
 */
class CryptoRepositoryImpl @Inject constructor(
    private val coinGeckoService: CoinGeckoService
) : CryptoRepository

/**
 * Implementazione di FavoritesRepository (Room).
 * Logica e metodi definiti nello Step 8.
 */
class FavoritesRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val cryptoDao: CryptoDao
) : FavoritesRepository
