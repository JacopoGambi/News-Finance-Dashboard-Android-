package com.example.newsfinance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.newsfinance.domain.model.Article

/**
 * Entità Room per gli articoli salvati nei preferiti.
 * La chiave primaria è l'url dell'articolo (stabile, univoco).
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val publishedAt: String?,
    val sourceName: String?,
    val category: String?
)

fun ArticleEntity.toDomain(): Article = Article(
    id = url,
    title = title,
    description = description,
    url = url,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    sourceName = sourceName,
    category = category
)

fun Article.toEntity(): ArticleEntity = ArticleEntity(
    url = url,
    title = title,
    description = description,
    imageUrl = imageUrl,
    publishedAt = publishedAt,
    sourceName = sourceName,
    category = category
)
