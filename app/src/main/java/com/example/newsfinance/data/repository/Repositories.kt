package com.example.newsfinance.data.repository

import com.example.newsfinance.data.local.dao.ArticleDao
import com.example.newsfinance.data.local.dao.CryptoDao
import com.example.newsfinance.domain.repository.FavoritesRepository
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val cryptoDao: CryptoDao
) : FavoritesRepository
