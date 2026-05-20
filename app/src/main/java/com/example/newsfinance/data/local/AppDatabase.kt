package com.example.newsfinance.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.newsfinance.data.local.dao.ArticleDao
import com.example.newsfinance.data.local.dao.CryptoDao
import com.example.newsfinance.data.local.entity.ArticleEntity
import com.example.newsfinance.data.local.entity.CryptoEntity

/**
 * Database Room dell'app.
 * Contiene articoli preferiti e crypto in watchlist.
 */
@Database(
    entities = [ArticleEntity::class, CryptoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun cryptoDao(): CryptoDao
}
