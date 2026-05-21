package com.example.newsfinance.data.remote.dto

import com.example.newsfinance.domain.model.Article
import com.google.gson.annotations.SerializedName

/**
 * Risposta top-level di NewsAPI.
 */
data class NewsResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("totalResults") val totalResults: Int?,
    @SerializedName("articles") val articles: List<ArticleDto>?
)

/**
 * Singolo articolo restituito da NewsAPI.
 */
data class ArticleDto(
    @SerializedName("source") val source: SourceDto?,
    @SerializedName("author") val author: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("urlToImage") val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("content") val content: String?
)

/**
 * Sorgente dell'articolo (id opzionale, name di solito presente).
 */
data class SourceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?
)

/**
 * Mapper DTO -> dominio.
 * NewsAPI non espone la categoria nel JSON: viene passata dal caller (repository/use case).
 * Articoli con url nullo vengono scartati (id = url).
 */
fun ArticleDto.toDomain(category: String? = null): Article? {
    val safeUrl = url ?: return null
    val safeTitle = title ?: return null
    if (safeTitle == "[Removed]") return null
    return Article(
        id = safeUrl,
        title = safeTitle,
        description = description,
        url = safeUrl,
        imageUrl = urlToImage,
        publishedAt = publishedAt,
        sourceName = source?.name,
        category = category
    )
}
