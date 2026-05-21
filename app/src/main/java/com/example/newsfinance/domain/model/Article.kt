package com.example.newsfinance.domain.model

/**
 * Modello di dominio per un articolo di notizie.
 * Indipendente dal layer remote/local.
 *
 * @property id identificatore stabile (url dell'articolo)
 * @property publishedAt timestamp ISO 8601 grezzo (parsing in UI se necessario)
 * @property category categoria assegnata dal caller (NewsAPI non la include nel JSON)
 */
data class Article(
    val id: String,
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAt: String?,
    val sourceName: String?,
    val category: String?
)
