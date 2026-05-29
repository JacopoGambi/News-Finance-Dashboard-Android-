package com.example.newsfinance.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.newsfinance.data.local.dao.AlertDao
import com.example.newsfinance.data.local.dao.ArticleDao
import com.example.newsfinance.data.local.dao.CryptoDao
import com.example.newsfinance.data.local.entity.AlertEntity
import com.example.newsfinance.data.local.entity.ArticleEntity
import com.example.newsfinance.data.local.entity.CryptoEntity

/**
 * Database Room dell'app.
 * Contiene articoli preferiti, crypto in watchlist e alert di prezzo.
 */
@Database(
    entities = [ArticleEntity::class, CryptoEntity::class, AlertEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun cryptoDao(): CryptoDao
    abstract fun alertDao(): AlertDao
}
