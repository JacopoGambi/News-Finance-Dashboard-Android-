package com.example.newsfinance.data.remote.dto

import com.example.newsfinance.domain.model.Article
import com.google.gson.annotations.SerializedName

/**
 * Risposta top-level di GNews.
 */
data class NewsResponseDto(
    @SerializedName("totalArticles") val totalArticles: Int?,
    @SerializedName("articles") val articles: List<ArticleDto>?
)

/**
 * Singolo articolo restituito da GNews.
 */
data class ArticleDto(
    @SerializedName("source") val source: SourceDto?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("content") val content: String?
)

/**
 * Sorgente dell'articolo (GNews espone name e url).
 */
data class SourceDto(
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

/**
 * Mapper DTO -> dominio.
 * GNews non espone la categoria nel JSON: viene passata dal caller (repository/use case).
 * Articoli con url nullo vengono scartati (id = url).
 */
fun ArticleDto.toDomain(category: String? = null): Article? {
    val safeUrl = url ?: return null
    val safeTitle = title ?: return null
    return Article(
        id = safeUrl,
        title = safeTitle,
        description = description,
        url = safeUrl,
        imageUrl = image,
        publishedAt = publishedAt,
        sourceName = source?.name,
        category = category
    )
}
