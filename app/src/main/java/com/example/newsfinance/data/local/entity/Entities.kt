package com.example.newsfinance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entità Room per gli articoli salvati nei preferiti.
 * Schema completo definito nello Step 7.
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String
)

/**
 * Entità Room per le crypto in watchlist con eventuale soglia di alert.
 * Schema completo definito nello Step 7.
 */
@Entity(tableName = "cryptos")
data class CryptoEntity(
    @PrimaryKey val id: String
)
